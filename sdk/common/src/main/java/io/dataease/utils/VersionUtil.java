package io.dataease.utils;

public class VersionUtil {

    private static String randomId = IDUtils.randomID(16);

    private static int time = 0;

    public static String getRandomVersion() {
        ++time;
        String result = randomId;
        if (time == 10) {
            randomId = null;
        }
        return result;
    }

}
