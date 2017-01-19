package com.lexing.batterytest.batteryhelper;

/**
 * Created by Ray on 2017/1/16.
 */

public abstract class Wrapper {

    private Object mWrappedObj;

    protected Wrapper(Object wrappedObj) {
        this.mWrappedObj = wrappedObj;
    }

    protected Object unwrap() {
        return mWrappedObj;
    }

    protected abstract String className();

    protected Object invoke(String methodName, Object... params) {
        try {
            if (params == null) {
                return ReflectHelper.invokeNoStaticMethod(unwrap(), methodName, null, null);
            }
            Class<?>[] classArray = new Class[params.length];
            for (int i = 0; i < params.length; i++) {
                classArray[i] = params[i].getClass();
            }
            return ReflectHelper.invokeNoStaticMethod(unwrap(), methodName, classArray, params);
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    protected Object invoke(String methodName, Class<?>[] classArray, Object[] params) {
        try {
            if (params == null) {
                return ReflectHelper.invokeNoStaticMethod(unwrap(), methodName, null, null);
            }
            return ReflectHelper.invokeNoStaticMethod(unwrap(), methodName, classArray, params);
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    protected static Object invokeStatic(String className, String methodName, Object... params) {
        try {
            if (params == null) {
                return ReflectHelper.invokeStaticMethod(className, methodName, null, null);
            }
            Class<?>[] classArray = new Class[params.length];
            for (int i = 0; i < params.length; i++) {
                classArray[i] = params[i].getClass();
            }
            return ReflectHelper.invokeStaticMethod(className, methodName, classArray, params);
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    protected static Object invokeStatic(String className, String methodName, Class<?>[] classArray, Object[] params) {
        try {
            if (params == null) {
                return ReflectHelper.invokeStaticMethod(className, methodName, null, null);
            }
            return ReflectHelper.invokeStaticMethod(className, methodName, classArray, params);
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    protected static void handleException(Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e);
    }
}
