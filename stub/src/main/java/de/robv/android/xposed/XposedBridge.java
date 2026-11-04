package de.robv.android.xposed;

import java.util.Set;

/** Stub for compile-time source compatibility. Never packaged. */
public final class XposedBridge {
    private XposedBridge() { }

    public static void log(final String text) {
        throw new UnsupportedOperationException("compile stub");
    }

    public static void log(final Throwable t) {
        throw new UnsupportedOperationException("compile stub");
    }

    public static Set<XC_MethodHook.Unhook> hookAllMethods(
            final Class<?> hookClass, final String methodName, final XC_MethodHook callback) {
        throw new UnsupportedOperationException("compile stub");
    }
}
