package com.minis.imelandfix;

import android.graphics.Rect;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * System-framework hook (scope: "android", loads inside system_server).
 *
 * Problem: on vivo/OriginOS (iQOO Neo10, Android 15) the window manager lays out
 * the IME window (type 2011) inside the display-cutout safe zone. In landscape the
 * punch-hole cutout becomes a left-edge inset of 125 px, so the keyboard window
 * starts at x=125 while normal apps (e.g. the streamed fullscreen window) start at
 * x=0. The keyboard therefore appears shifted and never reaches the left edge.
 * In AOSP the IME is exempt (cutout mode ALWAYS); the ROM contradicts that.
 *
 * Fix: in the DisplayPolicy layout entry point, after the window is laid out, if
 * the subject is an IME window on a rotated display and its frame is narrower than
 * the parent frame, widen the frame horizontally to the parent frame. The IME is
 * bottom-anchored by the window shadow, so widening the width does not move keys.
 *
 * v0.1: landscape (rotation 90/270) only; rotation 180 and portrait (0) untouched.
 * Fail-safe: any throwable inside the hook is contained and logged; layout of
 * all other window types is never modified.
 */
public final class MainHook implements IXposedHookLoadPackage {

    private static final String TAG = "ImeLandFix";
    private static final int TYPE_INPUT_METHOD_FIRST = 2011;

    /** DisplayPolicy layout entry names across AOSP/OEM versions; first match wins. */
    private static final String[] ENTRY_CANDIDATES = {
            "layoutWindowLw",   // AOSP android-12..15
            "applyLayoutLw",    // seen in some OEM forks
            "computeLayoutLw"   // legacy name (android-10/11 forks)
    };

    private static volatile boolean sHooked = false;
    private static volatile boolean sLoggedFirstFix = false;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam == null) return;
        if (!"android".equals(lpparam.packageName)) return;
        if (sHooked) return; // guard against duplicate loads in one process
        sHooked = true;

        hookByCandidates(lpparam.classLoader);
    }

    private static void hookByCandidates(final ClassLoader sysClassLoader) {
        final Class<?> dpClass;
        try {
            dpClass = XposedHelpers.findClass(
                    "com.android.server.wm.DisplayPolicy", sysClassLoader);
        } catch (Throwable t) {
            logE("DisplayPolicy not found; module inactive", t);
            return;
        }

        for (final String name : ENTRY_CANDIDATES) {
            try {
                final int n = XposedBridge.hookAllMethods(dpClass, name, sHook).size();
                if (n > 0) {
                    logI("hooked " + n + " method(s) named '" + name + "'");
                    return;
                }
            } catch (Throwable t) {
                logW("hookAllMethods('" + name + "') failed", t);
            }
        }
        logE("no layout entry point hooked; module inactive", null);
    }

    private static final XC_MethodHook sHook = new XC_MethodHook() {
        @Override
        protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
            try {
                final Object[] args = param.args;
                if (args == null || args.length == 0) return;
                final Object win = args[0];
                if (!isWindowState(win)) return;

                final Object attrs = XposedHelpers.getObjectField(win, "mAttrs");
                if (attrs == null
                        || XposedHelpers.getIntField(attrs, "type") != TYPE_INPUT_METHOD_FIRST) {
                    return;
                }

                final Object dc = XposedHelpers.callMethod(win, "getDisplayContent");
                if (dc == null) return;
                final int rot = tryGetRotation(dc);
                if (rot <= 0) return; // 0 = portrait (out of scope), negative = unknown

                // AOSP 12..15: WindowState holds a private final WindowFrames object;
                // there is NO getWindowFrames() — go via the field directly.
                final Object frames = XposedHelpers.getObjectField(win, "mWindowFrames");
                if (frames == null) return;
                final Rect frame = (Rect) XposedHelpers.getObjectField(frames, "mFrame");
                final Rect parent = (Rect) XposedHelpers.getObjectField(frames, "mParentFrame");
                if (frame == null || parent == null) return;

                // Idempotence + sanity: only widen when parent is really wider.
                if (parent.width() <= 0 || frame.width() >= parent.width()) return;

                final int oldL = frame.left, oldR = frame.right;
                frame.left = parent.left;
                frame.right = parent.right;
                // Height/top intentionally untouched: IME is bottom-anchored, widening
                // horizontally preserves the laid-out height.

                if (!sLoggedFirstFix && (oldL != frame.left || oldR != frame.right)) {
                    sLoggedFirstFix = true;
                    logI("IME widened: x[" + oldL + ".." + oldR + "] -> x["
                            + frame.left + ".." + frame.right + "], rot=" + rot);
                }
            } catch (final Throwable t) {
                // Never propagate into system_server layout traversal.
                logW("hook body aborted by throwable", t);
            }
        }
    };

    /** True if the object's class chain contains com.android.server.wm.WindowState. */
    private static boolean isWindowState(final Object o) {
        if (o == null) return false;
        final String want = "com.android.server.wm.WindowState";
        for (Class<?> c = o.getClass(); c != null; c = c.getSuperclass()) {
            if (want.equals(c.getName())) return true;
        }
        return false;
    }

    /** DisplayContent rotation via field mRotation, fallback to getRotation(). */
    private static int tryGetRotation(final Object dc) {
        try {
            return XposedHelpers.getIntField(dc, "mRotation");
        } catch (final Throwable ignored) { }
        try {
            final Object r = XposedHelpers.callMethod(dc, "getRotation");
            return (r instanceof Integer) ? ((Integer) r).intValue() : -1;
        } catch (final Throwable ignored) { }
        return -1;
    }

    private static void logI(final String msg) {
        try { XposedBridge.log(TAG + ": " + msg); } catch (final Throwable ignored) { }
    }

    private static void logW(final String msg, final Throwable t) {
        try {
            XposedBridge.log(TAG + ": " + msg);
            if (t != null) XposedBridge.log(t);
        } catch (final Throwable ignored) { }
    }

    private static void logE(final String msg, final Throwable t) {
        try {
            XposedBridge.log(TAG + ": " + msg);
            if (t != null) XposedBridge.log(t);
        } catch (final Throwable ignored) { }
    }
}
