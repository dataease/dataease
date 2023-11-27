package io.dataease.utils;

import cn.hutool.core.collection.ListUtil;
import io.dataease.constant.XpackSettingConstants;

import java.util.List;

public class SystemSettingUtils {

    public static boolean xpackSetting(String pkey) {
        List<String> xpackSettingList = ListUtil.toList(XpackSettingConstants.AUTO_CREATE_USER);
        return xpackSettingList.contains(pkey);
    }
}
