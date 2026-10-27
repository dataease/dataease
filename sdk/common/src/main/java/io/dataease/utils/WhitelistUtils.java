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

    private static final List<String> FRONTEND_RESOURCE_PATHS = List.of(
            "/assets/", "/js/", "/svg/", "/tinymce-dataease-private/", "/webjars/", "/swagger-ui/");

    private static final List<String> FRONTEND_RESOURCE_FILES = List.of(
            "/dataease.svg", "/api.png", "/vite.svg", "/favicon.ico");

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

    public static boolean match(String requestURI) {
        invalidUrl(requestURI);
        String context = StringUtils.removeEnd(getContextPath(), "/");
        if (hasPathPrefix(requestURI, context)) {
            requestURI = requestURI.substring(context.length());
        }
        boolean apiRequest = false;
        for (String prefix : List.of(AuthConstant.DE_API_PREFIX, AuthConstant.DE_CASAPI_PREFIX, AuthConstant.DE_OIDCAPI_PREFIX)) {
            if (hasPathPrefix(requestURI, prefix)) {
                requestURI = requestURI.substring(prefix.length());
                apiRequest = true;
                break;
            }
        }
        return WHITE_PATH.contains(requestURI)
                || (!apiRequest && isFrontendResource(requestURI))
                || StringUtils.startsWithAny(requestURI, "/login/platformLogin/")
                || StringUtils.startsWithAny(requestURI, "/appearance/image/")
                || StringUtils.startsWithAny(requestURI, "/share/proxyInfo")
                || StringUtils.startsWithAny(requestURI, "/share/validate")
                || StringUtils.startsWithAny(requestURI, "/xpackComponent/content")
                || StringUtils.startsWithAny(requestURI, "/xpackComponent/pluginStaticInfo")
                || StringUtils.startsWithAny(requestURI, "/geo/")
                || StringUtils.startsWithAny(requestURI, "/websocket")
                || StringUtils.startsWithAny(requestURI, "/map/")
                || StringUtils.startsWithAny(requestURI, "/oauth2/")
                || StringUtils.startsWithAny(requestURI, "/mfa/qr/")
                || StringUtils.startsWithAny(requestURI, "/mfa/login")
                || StringUtils.startsWithAny(requestURI, "/typeface/download")
                || StringUtils.startsWithAny(requestURI, "/typeface/defaultFont")
                || StringUtils.startsWithAny(requestURI, "/typeface/listFont")
                || StringUtils.startsWithAny(requestURI, "/exportCenter/download")
                || StringUtils.startsWithAny(requestURI, "/i18n/")
                || StringUtils.startsWithAny(requestURI, "/communicate/image/")
                || StringUtils.startsWithAny(requestURI, "/saml/")
                || StringUtils.startsWithAny(requestURI, "/communicate/down/");
    }

    private static boolean hasPathPrefix(String requestURI, String prefix) {
        return StringUtils.isNotBlank(prefix)
                && (requestURI.equals(prefix) || requestURI.startsWith(prefix + "/"));
    }

    private static boolean isFrontendResource(String requestURI) {
        // API path variables must never become public merely because they end in an image or script suffix.
        if (FRONTEND_RESOURCE_FILES.contains(requestURI)) {
            return true;
        }
        return FRONTEND_RESOURCE_PATHS.stream().anyMatch(requestURI::startsWith)
                && StringUtils.endsWithAny(requestURI, ".gif", ".ico", ".js", ".css", ".svg", ".png", ".jpg", ".jpeg",
                ".js.map", ".otf", ".ttf", ".woff", ".woff2");
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
