package io.dataease.utils;

public class CommunityUtils {

    private static final ThreadLocal<String> COMMUNITY_INFO = new ThreadLocal<>();

    public static void setInfo(String info) {
        COMMUNITY_INFO.set(info);
    }

    public static String getInfo() {
        return COMMUNITY_INFO.get();
    }
}
