package io.dataease.utils;

import io.dataease.constant.XpackSettingConstants;

import java.util.List;

public class SystemSettingUtils {

    public static boolean xpackSetting(String pkey) {

        List<String> xpackSettingList = List.of(XpackSettingConstants.AUTO_CREATE_USER, XpackSettingConstants.LOG_LIVE_TIME);
        return xpackSettingList.contains(pkey);
    }
}
