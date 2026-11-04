package de.robv.android.xposed;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/** Stub for compile-time source compatibility. Never packaged. */
public interface IXposedHookLoadPackage {
    void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable;
}
