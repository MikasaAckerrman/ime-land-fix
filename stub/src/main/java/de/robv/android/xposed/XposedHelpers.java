package de.robv.android.xposed;

/** Stub for compile-time source compatibility. Never packaged. */
public final class XposedHelpers {
    private XposedHelpers() { }

    public static Class<?> findClass(final String className, final ClassLoader classLoader) {
        throw new UnsupportedOperationException("compile stub");
    }

    public static Object getObjectField(final Object obj, final String fieldName) {
        throw new UnsupportedOperationException("compile stub");
    }

    public static int getIntField(final Object obj, final String fieldName) {
        throw new UnsupportedOperationException("compile stub");
    }

    public static Object callMethod(
            final Object obj, final String methodName, final Object... args) {
        throw new UnsupportedOperationException("compile stub");
    }
}
