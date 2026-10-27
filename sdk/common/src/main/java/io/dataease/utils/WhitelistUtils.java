package io.dataease.utils;

import io.dataease.constant.AuthConstant;
import io.dataease.exception.DEException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.Objects;

import static io.dataease.result.ResultCode.INTERFACE_ADDRESS_INVALID;

public class WhitelistUtils {

    private static String contextPath;


    public static String getContextPath() {
        if (StringUtils.isBlank(contextPath)) {
            contextPath = Objects.requireNonNull(CommonBeanFactory.getBean(Environment.class)).getProperty("server.servlet.context-path", String.class);
        }
        return contextPath;
    }

    public static List<String> WHITE_PATH = List.of(
            "/login/localLogin",
            "/apisix/check",
            "/dekey",
            "/index.html",
            "/model",
            "/xpackModel",
            "/swagger-resources",
            "/doc.html",
            "/panel.html",
            "/mobile.html",
            "/lark/qrinfo",
            "/lark/token",
            "/larksuite/qrinfo",
            "/larksuite/token",
            "/dingtalk/qrinfo",
            "/dingtalk/token",
            "/wecom/qrinfo",
            "/wecom/token",
            "/sysParameter/requestTimeOut",
            "/sysParameter/defaultSettings",
            "/setting/authentication/status",
            "/sysParameter/ui",
            "/sysParameter/defaultLogin",
            "/embedded/initIframe",
            "/sysParameter/i18nOptions",
            "/login/modifyInvalidPwd",
            "/perSetting/hmac/info",
            "/");

    // TinyMCE loads its language and skin files through browser tags without authentication headers.
    private static final String[] FRONTEND_STATIC_PREFIXES = {
            "/js/", "/assets/", "/tinymce-dataease-private/"};

    private static final String[] FRONTEND_STATIC_SUFFIXES = {
            ".js", ".css", ".svg", ".png", ".jpg", ".ico", ".js.map", ".otf", ".ttf", ".woff2"};

    // Knife4j loads these resources through script/link tags without authentication headers.
    private static final String[] WEBJARS_STATIC_SUFFIXES = {
            ".js", ".css", ".svg", ".png", ".jpg", ".gif", ".ico", ".otf", ".ttf", ".woff", ".woff2", ".eot"};

    private static boolean matchFrontendStaticResource(String requestURI) {
        String path = StringUtils.substringBefore(requestURI, "?");
        path = StringUtils.substringBefore(path, ";");
        return (StringUtils.startsWithAny(path, FRONTEND_STATIC_PREFIXES)
                && StringUtils.endsWithAny(path, FRONTEND_STATIC_SUFFIXES))
                || (StringUtils.startsWith(path, "/webjars/")
                && StringUtils.endsWithAny(path, WEBJARS_STATIC_SUFFIXES));
    }

    public static boolean match(String requestURI) {
        invalidUrl(requestURI);
        if (StringUtils.startsWith(requestURI, getContextPath())) {
            requestURI = StringUtils.replaceOnce(requestURI, getContextPath(), "");
        }
        if (StringUtils.startsWith(requestURI, AuthConstant.DE_API_PREFIX)) {
            requestURI = requestURI.replaceFirst(AuthConstant.DE_API_PREFIX, "");
        }
        if (StringUtils.startsWith(requestURI, AuthConstant.DE_CASAPI_PREFIX)) {
            requestURI = requestURI.replaceFirst(AuthConstant.DE_CASAPI_PREFIX, "");
        }
        if (StringUtils.startsWith(requestURI, AuthConstant.DE_OIDCAPI_PREFIX)) {
            requestURI = requestURI.replaceFirst(AuthConstant.DE_OIDCAPI_PREFIX, "");
        }
        return WHITE_PATH.contains(requestURI)
                || matchFrontendStaticResource(requestURI)
                || StringUtils.startsWithAny(requestURI, "data:image")
                || StringUtils.startsWithAny(requestURI, "/login/platformLogin/")
                || StringUtils.startsWithAny(requestURI, "/static-resource/")
                || StringUtils.startsWithAny(requestURI, "/appearance/image/")
                || StringUtils.startsWithAny(requestURI, "/share/proxyInfo")
                || StringUtils.startsWithAny(requestURI, "/share/validate")
                || StringUtils.startsWithAny(requestURI, "/xpackComponent/contentPlugin/")
                || StringUtils.startsWithAny(requestURI, "/xpackComponent/pluginStaticInfo")
                || StringUtils.startsWithAny(requestURI, "/geo/")
                || StringUtils.startsWithAny(requestURI, "/websocket")
                || StringUtils.startsWithAny(requestURI, "/map/")
                || StringUtils.startsWithAny(requestURI, "/oauth2/")
                || StringUtils.startsWithAny(requestURI, "/mfa/qr/")
                || StringUtils.equals(requestURI, "/mfa/login")
                || StringUtils.startsWithAny(requestURI, "/typeface/download")
                || StringUtils.startsWithAny(requestURI, "/typeface/defaultFont")
                || StringUtils.startsWithAny(requestURI, "/typeface/listFont")
                || StringUtils.startsWithAny(requestURI, "/exportCenter/download")
                || StringUtils.startsWithAny(requestURI, "/i18n/")
                || StringUtils.startsWithAny(requestURI, "/communicate/image/")
                || StringUtils.startsWithAny(requestURI, "/saml/")
                || StringUtils.startsWithAny(requestURI, "/communicate/down/");
    }

    public static String getBaseApiUrl(String redirect_uri) {
        if (StringUtils.endsWith(redirect_uri, "/")) {
            redirect_uri = redirect_uri.substring(0, redirect_uri.length() - 1);
        }
        String contextPath = WhitelistUtils.getContextPath();
        if (StringUtils.isNotBlank(contextPath)) {
            redirect_uri += contextPath;
        }
        return redirect_uri + AuthConstant.DE_API_PREFIX + "/";
    }

    private static void invalidUrl(String requestURI) {
        if (requestURI.contains("./") || requestURI.contains(".%") || requestURI.toLowerCase().contains("%2e") || (requestURI.contains(";") && !requestURI.contains("?"))) {
            DEException.throwException(INTERFACE_ADDRESS_INVALID.code(), String.format("%s [%s]", INTERFACE_ADDRESS_INVALID.message(), requestURI));
        }
    }
}
