package io.dataease.commons.holder;

public class ThreadLocalContextHolder {


    private static ThreadLocal<Object> sceneThreadLocal = new ThreadLocal<>();


    public static Object getData() {
        return sceneThreadLocal.get();
    }

    public static void setData(Object data) {
        if (ThreadLocalContextHolder.sceneThreadLocal == null) {
            ThreadLocalContextHolder.sceneThreadLocal = new ThreadLocal<>();
        }
        ThreadLocalContextHolder.sceneThreadLocal.set(data);
    }

    public static void clearScene() {
        setData(null);
    }

}
