package com.lexing.batterytest.batteryhelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class ReflectHelper {
    /**
     * 创建类的实例，调用类的无参构造方法
     */
    public static Object newInstance(String className) throws ClassNotFoundException,
            IllegalAccessException, InstantiationException {
        Object instance = null;
        Class<?> clazz = Class.forName(className);
        instance = clazz.newInstance();
        return instance;
    }

    /**
     * 获取所有的public构造方法的信息
     */
    public static String getPublicConstructorInfo(String className) throws ClassNotFoundException {
        StringBuilder sBuilder = new StringBuilder();
        Class<?> clazz = Class.forName(className);
        Constructor<?>[] constructors = clazz.getConstructors();
        sBuilder.append(getConstructorInfo(constructors));
        return sBuilder.toString();
    }

    /**
     * 得到本类内声明的构造方法信息
     */
    public static String getDeclearedConstructorInfo(String className) throws ClassNotFoundException {
        StringBuilder sBuilder = new StringBuilder();
        Class<?> clazz = Class.forName(className);
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        sBuilder.append(getConstructorInfo(constructors));
        return sBuilder.toString();
    }

    /**
     * 获取public的字段信息
     */
    public static String getPublicFieldInfo(String className) throws ClassNotFoundException {
        StringBuilder sBuilder = new StringBuilder();
        Class<?> clazz = Class.forName(className);
        Field[] fields = clazz.getFields();
        sBuilder.append(getFieldInfo(fields));
        return sBuilder.toString();
    }

    /**
     * 获取本类内声明的字段信息
     */
    public static String getDecleardFieldInfo(String className) throws ClassNotFoundException {
        StringBuilder sBuilder = new StringBuilder();
        Class<?> clazz = Class.forName(className);
        Field[] fields = clazz.getDeclaredFields();
        sBuilder.append(getFieldInfo(fields));
        return sBuilder.toString();
    }

    /**
     * 得到所有public方法信息
     */
    public static String getPublicMethodInfos(String className) throws ClassNotFoundException {
        StringBuilder sBuilder = new StringBuilder();
        Class<?> clazz = Class.forName(className);
        Method[] methods = clazz.getMethods();
        // 得到所有的public方法，包括从基类继承的
        sBuilder.append(getMethodInfo(methods));
        return sBuilder.toString();
    }

    /**
     * 得到类内声明的方法信息
     */
    public static String getDeclaredMethodInfos(String className) throws ClassNotFoundException {
        StringBuilder sBuilder = new StringBuilder();
        Class<?> clazz = Class.forName(className);
        Method[] methods = clazz.getDeclaredMethods();
        // 得到本类声明的所有方法,包括私有方法
        // clazz.getMethods(); 会返回所有public的方法，但是包括基类Object的方法
        sBuilder.append(getMethodInfo(methods));
        return sBuilder.toString();
    }

    /**
     * 得到构造器信息
     */
    private static String getConstructorInfo(Constructor<?> constructor) {
        return ("name: " + constructor.getName()) +
                "/ngetParameterTypes: " + Arrays.toString(constructor.getParameterTypes());
    }

    /**
     * 将一组构造器的信息组成一个字符串返回
     */
    private static String getConstructorInfo(Constructor<?>[] constructors) {
        StringBuilder sBuilder = new StringBuilder();
        int i = 0;
        for (Constructor<?> c : constructors) {
            sBuilder.append("method: ").append(++i).append(" : ");
            sBuilder.append("/n").append(getConstructorInfo(c));
            sBuilder.append("/n");
        }
        return sBuilder.toString();
    }

    /**
     * 获取字段信息，组成一个字符串返回
     */
    private static String getFieldInfo(Field field) {
        return ("name: " + field.getName()) +
                "/ngetType: " + field.getType() +
                getModifiersInfo(field);
    }

    /**
     * 获取一组字段的信息，返回字符串
     */
    private static String getFieldInfo(Field[] fields) {
        StringBuilder sBuilder = new StringBuilder();
        int i = 0;
        for (Field field : fields) {
            sBuilder.append("field: ").append(++i).append(" : ");
            sBuilder.append("/n").append(getFieldInfo(field));
            sBuilder.append("/n");
        }
        return sBuilder.toString();
    }

    /**
     * 获取方法的信息，组成一个字符串返回
     */
    private static String getMethodInfo(Method method) {
        return ("name: " + method.getName()) +
                "/ngetReturnType: " + method.getReturnType() +
                "/ngetParameterTypes: " + Arrays.toString(method.getParameterTypes()) +
                getModifiersInfo(method);
    }

    /**
     * 获取一组方法的信息，组成一个字符串返回
     */
    private static String getMethodInfo(Method[] methods) {
        StringBuilder sBuilder = new StringBuilder();
        int i = 0;
        for (Method method : methods) {
            sBuilder.append("method: ").append(++i).append(" : ");
            sBuilder.append("/n").append(getMethodInfo(method));
            sBuilder.append("/n");
        }
        return sBuilder.toString();
    }

    /**
     * 获取修饰符信息
     */
    private static String getModifiersInfo(Member member) {
        StringBuilder sBuilder = new StringBuilder();
        int modifiers = member.getModifiers();
        sBuilder.append("/ngetModifiers: ").append(+modifiers).append(", ");
        // 得到修饰符编码
        sBuilder.append("/nisPublic: ").append(Modifier.isPublic(modifiers)).append(", ");
        sBuilder.append("/nisPrivate: ").append(Modifier.isPrivate(modifiers)).append(", ");
        sBuilder.append("/nisStatic: ").append(Modifier.isStatic(modifiers)).append(", ");
        sBuilder.append("/nisFinal: ").append(Modifier.isFinal(modifiers)).append(", ");
        sBuilder.append("/nisAbstract: ").append(Modifier.isAbstract(modifiers));
        return sBuilder.toString();
    }

    /**
     * 是否是公用静态方法
     */
    private static boolean isStatic(Member member) {
        int mod = member.getModifiers();
        return Modifier.isStatic(mod);
    }

    /**
     * 调用静态方法
     */
    public static Object invokeStaticMethod(String className, String methodName, Class<?>[] paramTypes, Object[] params)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> cls = Class.forName(className);
        Method method;
        try{
            method = cls.getDeclaredMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e){
            method = cls.getDeclaredMethod(methodName, getParameterTypes(paramTypes));
        }
        method.setAccessible(true);
        return method.invoke(null, params);
    }

    public static Object invokeNoStaticMethod(Object obj, String methodName, Class<?>[] paramTypes, Object[] params)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // 注意不要用getMethod(),因为getMethod()返回的都是public方法
        Method method = getDeclaredMethod(obj, methodName, paramTypes);
        method.setAccessible(true);
        // 抑制Java的访问控制检查
        return method.invoke(obj, params);
    }

    public static Class[] getParameterTypes(Class<?>[] args) {
        if(args == null){
            return null;
        }
        Class[] parameterTypes = new Class[args.length];
        for (int i = 0, j = args.length; i < j; i++) {
            if(args[i] == Integer.class){
                parameterTypes[i] = Integer.TYPE;
            }else if(args[i] == Byte.class){
                parameterTypes[i] = Byte.TYPE;
            }else if(args[i] == Short.class){
                parameterTypes[i] = Short.TYPE;
            }else if(args[i] == Float.class){
                parameterTypes[i] = Float.TYPE;
            }else if(args[i] == Double.class){
                parameterTypes[i] = Double.TYPE;
            }else if(args[i] == Character.class){
                parameterTypes[i] = Character.TYPE;
            }else if(args[i] == Long.class){
                parameterTypes[i] = Long.TYPE;
            }else if(args[i] == Boolean.class){
                parameterTypes[i] = Boolean.TYPE;
            }else{
                parameterTypes[i] = args[i];
            }
        }
        return parameterTypes;
    }

    public static Method getDeclaredMethod(Object obj, String methodName, Class<?>[] paramTypes)
            throws NoSuchMethodException{
        NoSuchMethodException e = null;
        for(Class<?> clazz = obj.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()){
            try{
                return clazz.getDeclaredMethod(methodName, paramTypes);
            } catch (NoSuchMethodException e1){
                try{
                    return clazz.getDeclaredMethod(methodName, getParameterTypes(paramTypes));
                } catch (NoSuchMethodException e2){
                    e = e2;
                }
            }
        }
        throw e;
    }
}