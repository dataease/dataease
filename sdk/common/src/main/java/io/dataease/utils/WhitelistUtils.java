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
            "/demo.html",
            "/swagger-resources",
            "/doc.html",
            "/panel.html",
            "/");

    public static boolean match(String requestURI) {
        if (StringUtils.startsWith(requestURI, AuthConstant.DE_API_PREFIX)) {
            requestURI = requestURI.replaceFirst(AuthConstant.DE_API_PREFIX, "");
        }
        return WHITE_PATH.contains(requestURI)
                || StringUtils.endsWithAny(requestURI, ".ico", "js", ".css", "svg", "png", "js.map")
                || StringUtils.startsWithAny(requestURI, "data:image")
                || StringUtils.startsWithAny(requestURI, "/v3/")
                || StringUtils.startsWithAny(requestURI, "/login/platformLogin/")
                || StringUtils.startsWithAny(requestURI, "/test/")
                || StringUtils.startsWithAny(requestURI, "/static-resource/");
    }
}
