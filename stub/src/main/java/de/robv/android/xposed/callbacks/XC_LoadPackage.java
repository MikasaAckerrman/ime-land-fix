package de.robv.android.xposed.callbacks;

/** Stub for compile-time source compatibility. Never packaged. */
public class XC_LoadPackage {
    public static final class LoadPackageParam {
        public String packageName;
        public String processName;
        public ClassLoader classLoader;
        public android.content.pm.ApplicationInfo appInfo;
        public boolean isFirstApplication;
    }
}
