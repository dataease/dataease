package io.dataease.provider;

import org.apache.commons.lang3.StringUtils;

public class Utils {
    public static boolean joinSort(String sort) {
        return (StringUtils.equalsIgnoreCase(sort, "asc") || StringUtils.equalsIgnoreCase(sort, "desc"));
    }
}
