package de.robv.android.xposed;

/** Stub for compile-time source compatibility. Never packaged. */
public abstract class XC_MethodHook {
    public XC_MethodHook() { }
    public XC_MethodHook(final int priority) { }

    protected void beforeHookedMethod(final MethodHookParam param) throws Throwable { }
    protected void afterHookedMethod(final MethodHookParam param) throws Throwable { }

    public static class MethodHookParam {
        public Object[] args;
        public java.lang.reflect.Member method;
        public Object thisObject;
        public Object result;
        public Throwable throwable;
    }

    public class Unhook {
        public void unhook() { }
    }
}
