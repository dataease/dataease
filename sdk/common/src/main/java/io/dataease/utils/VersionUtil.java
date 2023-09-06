package io.dataease.utils;

public class VersionUtil {

    private static final String randomId = IDUtils.randomID(16);

    public static String getRandomVersion() {
        return randomId;
    }

}
