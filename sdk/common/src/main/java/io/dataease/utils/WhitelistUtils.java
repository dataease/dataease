package io.dataease.utils;

import cn.hutool.core.collection.ListUtil;
import io.dataease.constant.AuthConstant;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class WhitelistUtils {

    public static List<String> WHITE_PATH = ListUtil.of(
            "/login/localLogin",
            "/apisix/check",
            "/dekey",
            "/index.html",
            "/model",
            "/swagger-resources",
            "/doc.html",
            "/panel.html",
            "/lark/info",
            "/lark/token",
            "/setting/authentication/status",
            "/");

    public static boolean match(String requestURI) {
        if (StringUtils.startsWith(requestURI, AuthConstant.DE_API_PREFIX)) {
            requestURI = requestURI.replaceFirst(AuthConstant.DE_API_PREFIX, "");
        }
        return WHITE_PATH.contains(requestURI)
                || StringUtils.endsWithAny(requestURI, ".ico", "js", ".css", "svg", "png", "jpg", "js.map", ".otf", ".ttf", ".woff2")
                || StringUtils.startsWithAny(requestURI, "data:image")
                || StringUtils.startsWithAny(requestURI, "/login/platformLogin/")
                || StringUtils.startsWithAny(requestURI, "/static-resource/")
                || StringUtils.startsWithAny(requestURI, "/share/proxyInfo")
                || StringUtils.startsWithAny(requestURI, "/xpackComponent/content/")
                || StringUtils.startsWithAny(requestURI, "/geo/")
                || StringUtils.startsWithAny(requestURI, "/map/");
    }
}
