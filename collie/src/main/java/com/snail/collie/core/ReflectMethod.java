package com.snail.collie.core;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectMethod {
    private static final String TAG = "ReflectFiled";
    private Class<?> mClazz;
    private String mMethodName;

    private boolean mInit;
    private Method mMethod;
    private Class[] mParameterTypes;

    public ReflectMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        if (clazz == null || methodName == null || methodName.length() == 0) {
            throw new IllegalArgumentException("Both of invoker and fieldName can not be null or nil.");
        }
        this.mClazz = clazz;
        this.mMethodName = methodName;
        this.mParameterTypes = parameterTypes;
    }

    private synchronized void prepare() {
        if (mInit) {
            return;
        }
        Class<?> clazz = mClazz;
        while (clazz != null) {
            try {
                Method method = clazz.getDeclaredMethod(mMethodName, mParameterTypes);
                method.setAccessible(true);
                mMethod = method;
                break;
            } catch (Exception e) {
            }
            clazz = clazz.getSuperclass();
        }
        mInit = true;
    }

    public synchronized <T> T invoke(Object instance, Object... args) throws NoSuchFieldException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        return invoke(instance, false, args);
    }

    public synchronized <T> T invoke(Object instance, boolean ignoreFieldNoExist, Object... args)
            throws NoSuchFieldException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        prepare();
        if (mMethod == null) {
            if (!ignoreFieldNoExist) {
                throw new NoSuchFieldException("Method " + mMethodName + " is not exists.");
            }
            return null;
        }
        return (T) mMethod.invoke(instance, args);
    }

}
