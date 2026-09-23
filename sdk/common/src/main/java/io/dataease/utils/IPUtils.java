package io.dataease.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.util.Arrays;

public class IPUtils {

    private static final String HEAD_KEYS = "x-forwarded-for, Proxy-Client-IP, WL-Proxy-Client-IP";

    private static final String UNKNOWN = "unknown";

    private static final String LOCAL_IP_KEY = "0:0:0:0:0:0:0:1";
    private static final String LOCAL_IP_VAL = "127.0.0.1";

    // 请求上下文(RequestContextHolder)基于ThreadLocal, 在异步/线程池的子线程中无法获取当前请求.
    // 对于这类场景, 在提交任务前于请求线程上取好IP并通过set()带入子线程, get()在拿不到请求时回退读取此处.
    private static final ThreadLocal<String> IP_HOLDER = new ThreadLocal<>();

    public static void set(String ip) {
        IP_HOLDER.set(ip);
    }

    public static void remove() {
        IP_HOLDER.remove();
    }

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
        if (ObjectUtils.isEmpty(request)) return IP_HOLDER.get();
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
