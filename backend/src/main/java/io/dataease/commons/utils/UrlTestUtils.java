package io.dataease.commons.utils;

import java.net.URL;
import java.net.URLConnection;

public class UrlTestUtils {

    public static boolean testUrlWithTimeOut(String urlString, int timeOutMillSeconds) {
        try {
            URL url = new URL(urlString);
            URLConnection co = url.openConnection();
            co.setConnectTimeout(timeOutMillSeconds);
            co.connect();
            return true;
        } catch (Exception e) {
            LogUtil.error(e);
            return false;
        }
    }
}
