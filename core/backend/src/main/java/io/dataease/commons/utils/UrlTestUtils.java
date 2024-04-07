package io.dataease.commons.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
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

    public static boolean isURLAvailable(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            return false;
        }
    }
}
