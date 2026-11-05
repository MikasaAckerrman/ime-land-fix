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

## Defect log (review 05.11)

- D1 (fatal): hook used non-existent `WindowState.getWindowFrames()` on AOSP 15 → replaced with direct `mWindowFrames` field access (AOSP15 removed the method). Verified against aosp-mirror android-15.0.0_r3 sources.
- D2 (risk): entry-point candidate list contained generic name `layout` which could hook unrelated methods → removed; candidates now `layoutWindowLw/applyLayoutLw/computeLayoutLw`.
- D3 (CI-fatal): root `build.gradle` declared only the `application` plugin version while `:stub` uses `com.android.library` → plugin resolution failed instantly; declared both at same version.
- D4 (verification-gap): APK contains TWO dex files (`classes.dex`=R stub, `classes2.dex`=hook classes); initial single-dex verification false-negatived the module. Verified via classes2 scan + utf-16 manifest scan: MainHook present, `layoutWindowLw` present, xposedmetadata present.

## CI

GitHub Actions builds on every push to `main`; artifact `ime-land-fix-debug-apk`. Runner's preinstalled SDK is used (no `setup-android` — that action fails against current runner images with "Failed to find package 'tools'").
