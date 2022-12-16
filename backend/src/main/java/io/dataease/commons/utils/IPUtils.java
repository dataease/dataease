package io.dataease.commons.utils;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.util.Arrays;

public class IPUtils {

    private static final String HEAD_KEYS = "x-forwarded-for, Proxy-Client-IP, WL-Proxy-Client-IP";

    private static final String UNKNOWN = "unknown";

    private static final String LOCAL_IP_KEY = "0:0:0:0:0:0:0:1";
    private static final String LOCAL_IP_VAL = "127.0.0.1";

    public static String get() {

        String ipStr = null;
        boolean isProxy = false;

        HttpServletRequest request = null;
        try {
            request = ServletUtils.request();
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            return null;
        }
        String[] keyArr = HEAD_KEYS.split(",");
        for (String key : keyArr) {
            String header = request.getHeader(key.trim());
            if (StringUtils.isNotBlank(header) && !StringUtils.equalsIgnoreCase(UNKNOWN, header)) {
                ipStr = header;
                isProxy = true;
                break;
            }
        }

        if (!isProxy) {
            ipStr = request.getRemoteAddr();
        }
        ipStr = Arrays.stream(ipStr.split(",")).filter(item -> StringUtils.isNotBlank(item) && !StringUtils.equalsIgnoreCase(UNKNOWN, item.trim())).findFirst().orElse(ipStr);
        return StringUtils.equals(LOCAL_IP_KEY, ipStr) ? LOCAL_IP_VAL : ipStr;
    }

    public static String domain() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return LOCAL_IP_VAL;
        }
    }
}
