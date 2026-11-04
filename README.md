# ime-land-fix — IME Landscape Fix

LSPosed module (scope: **Android System / framework**) that makes the keyboard
(IME, window type 2011) use the **full screen width in landscape** on vivo /
OriginOS (tested target: iQOO Neo10 / V2425A, Android 15 — the ROM trims the IME
window by the display-cutout inset, e.g. 125 px under the punch-hole, which AOSP
itself exempts IMEs from).

## What it does

Hooks `WindowManagerService`'s `DisplayPolicy` layout entry
(`layoutWindowLw` in AOSP android-12..15, with fallbacks for OEM forks) and, for
IME windows on rotated displays only, widens the laid-out window frame
horizontally to the parent frame so the keyboard can cover the full screen.

- Landscape (rotation 90/270): fixed — no dead column under the side cutout.
- Portrait / rotation 180: untouched in v0.1.
- Any runtime error inside the hook is contained and logged; other window types
  are never modified.

## Install

1. Get the APK:
   - CI build: GitHub Actions → `Build` run → artifact `ime-land-fix-debug-apk`
   - or `gradle :app:assembleDebug` (a `debug.keystore` is generated in CI).
2. Install APK on the device (debug-signed, for local testing only).
3. Enable the module in LSPosed, scope: **Android System (Android system
   framework)**.
4. Force-system_server restart (LSPosed "Restart System" / reboot).
5. Rotate to landscape and open the keyboard — it should now span the full width.

## Verify / logs

LSPosed → Logs → filter `ImeLandFix`:

- `hooked N method(s) named 'layoutWindowLw'` — entry point attached.
- `IME widened: x[125..2800] -> x[0..2800], rot=1` — a window was fixed.
- `...; module inactive` — wrong ROM state; check that the module is enabled and
  within scope (framework).

## Not in scope

- GBoard floating/resizing behaviour (a separate GBoard-side setting).
- Portrait layout trimming (may be added later as an option).
