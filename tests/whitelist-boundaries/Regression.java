import io.dataease.exception.DEException;
import io.dataease.permission.config.AuthFilter;
import io.dataease.permission.template.LoginTokenVerifier;
import io.dataease.permission.util.ModelUtils;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.utils.WhitelistUtils;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Regression {
    private static int assertions;

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        assertions++;
    }

    private static void match(String path, boolean expected) {
        check(WhitelistUtils.match(path) == expected, "Whitelist: " + path);
    }

    private static void filter(String method, String path, String token, boolean expected, Long uid) throws Exception {
        var request = new MockHttpServletRequest(method, path);
        if (token != null) request.addHeader("X-DE-TOKEN", token);
        var response = new MockHttpServletResponse();
        boolean[] reached = {false};
        new AuthFilter().doFilter(request, response, (req, res) -> {
            reached[0] = true;
            check(java.util.Objects.equals(uid, V3UserUtil.getUid()), "Bound identity: " + path);
        });
        check(reached[0] == expected, "Filter chain: " + path);
        check(response.getStatus() == (expected ? 200 : 401), "Filter status: " + path);
        check(V3UserUtil.getUid() == null && V3UserUtil.getLink() == null, "Identity cleanup");
    }

    public static void main(String[] args) throws Exception {
        try (var context = new GenericApplicationContext()) {
            context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                    "server.servlet.context-path", "", "dataease.version", "test")));
            context.registerBean(LoginTokenVerifier.class, () -> (token, request) -> {
                if (!"valid-test-token".equals(token)) throw new IllegalArgumentException("Invalid test token");
                return 42L;
            });
            context.refresh();
            new CommonBeanFactory().setApplicationContext(context);
            new io.dataease.license.utils.CommonBeanFactory().setApplicationContext(context);
            var contextPath = WhitelistUtils.class.getDeclaredField("contextPath");
            contextPath.setAccessible(true);
            match("/de2api/typeface/listFontOther", false);

            var publicPaths = List.of("/typeface/defaultFont", "/typeface/listFont", "/share/proxyInfo",
                    "/share/validate", "/map/worldTree", "/map/000/000.json", "/map/156/156.json",
                    "/typeface/download/font.woff2", "/exportCenter/download/123",
                    "/xpackComponent/pluginStaticInfo/chart-plugin", "/websocket", "/websocket/info",
                    "/websocket/000/session/xhr_streaming", "/oauth2/auth", "/oauth2/token",
                    "/saml/metadata", "/saml/login", "/saml/sso", "/login/localLogin",
                    "/login/platformLogin/test", "/embedded/initIframe", "/sysParameter/ui",
                    "/sysParameter/defaultSettings", "/geo/156/custom.json", "/i18n/zh-CN.json",
                    "/communicate/image/123", "/communicate/down/123/report/pdf",
                    "/assets/app.js", "/tinymce-dataease-private/skins/ui/oxide/skin.css");
            var protectedPaths = List.of("/typeface/defaultFontOther", "/typeface/defaultFont/other",
                    "/typeface/listFontOther", "/typeface/listFont/other", "/share/proxyInfoOther",
                    "/share/validateOther", "/share/validate/other", "/typeface/settings", "/typeface/create",
                    "/typeface/downloadOther/file", "/typeface/download", "/typeface/download/",
                    "/typeface/download/font/other", "/exportCenter/downloadOther/123",
                    "/exportCenter/download", "/exportCenter/download/123/other",
                    "/xpackComponent/pluginStaticInfoOther/name", "/xpackComponent/pluginStaticInfo/",
                    "/xpackComponent/pluginStaticInfo/name/other", "/websocketOther", "/websocketOther/info",
                    "/map/worldTreeOther", "/map/worldTree/other", "/map/admin", "/map/config.json",
                    "/map/156/", "/map/156/.json", "/map//156.json", "/map/156/156.json/other",
                    "/map/156/156.json.bak", "/map/156/156/config.json", "/map/156/config.yml",
                    "/user/personInfo", "/sysParameter/basic/query", "/chart/getData");

            for (String deployment : List.of("", "/tenant")) {
                contextPath.set(null, deployment);
                for (String api : List.of("", "/de2api", "/casbi/de2api", "/oidcbi/de2api")) {
                    String prefix = deployment + api;
                    for (String path : publicPaths) {
                        match(prefix + path, true);
                        match(prefix + path + "?v=1&ticket=test", true);
                    }
                    for (String path : protectedPaths) {
                        match(prefix + path, false);
                        match(prefix + path + "?v=1", false);
                    }
                }
            }
            contextPath.set(null, "");
            for (String path : List.of("/map/../156.json", "/map/%2e%2e/156.json",
                    "/typeface/listFont;other")) {
                try {
                    WhitelistUtils.match(path);
                    throw new AssertionError("Invalid URL accepted: " + path);
                } catch (DEException expected) {
                    assertions++;
                }
            }

            int mapFiles = 0;
            try (var files = Files.walk(Path.of(args[0]))) {
                for (Path file : files.filter(Files::isRegularFile).toList()) {
                    match("/de2api/map/" + Path.of(args[0]).relativize(file).toString().replace('\\', '/'), true);
                    mapFiles++;
                }
            }
            check(mapFiles > 0, "Map resource inventory is nonempty");

            Set<String> publicControllers = Set.of("/map/worldTree", "/typeface/listFont",
                    "/typeface/defaultFont", "/typeface/download/{file}", "/share/proxyInfo", "/share/validate",
                    "/exportCenter/download/{id}", "/xpackComponent/contentPlugin/{name}",
                    "/xpackComponent/pluginStaticInfo/{moduleName}");
            List<String> routes = Files.readAllLines(Path.of(args[1]));
            check(routes.size() > 30, "Controller inventory is nonempty");
            for (String route : routes) {
                // Sample both ordinary identifiers and resource-like paths in parameter mappings.
                for (String value : List.of("sample", "sample.json", "sample/extra.json")) {
                    String path = route.replaceAll("\\{[^}]+}", value);
                    if (publicControllers.contains(route) && value.contains("/")) continue;
                    match("/de2api" + path, publicControllers.contains(route));
                }
            }

            for (String mode : List.of("standalone", "distributed")) {
                new ModelUtils().setModelValue(mode);
                for (String path : List.of("/de2api/typeface/defaultFont", "/de2api/typeface/listFont",
                        "/de2api/map/worldTree", "/de2api/map/156/156.json", "/de2api/oauth2/auth",
                        "/de2api/saml/sso", "/de2api/exportCenter/download/123")) {
                    filter(path.endsWith("listFont") || path.endsWith("sso") ? "POST" : "GET",
                            path, null, true, null);
                }
                for (String path : List.of("/de2api/typeface/listFontOther", "/de2api/map/admin",
                        "/de2api/share/validateOther", "/de2api/typeface/settings")) {
                    filter("GET", path, null, false, null);
                    filter("GET", path, "invalid-test-token", false, null);
                    filter("GET", path, "valid-test-token", true, 42L);
                }
            }
            new ModelUtils().setModelValue("desktop");
            filter("GET", "/de2api/map/admin", null, true, 1L);
            System.out.println("PASS: " + assertions + " assertions; " + mapFiles
                    + " map files; " + routes.size() + " SDK route samples; real SDK Filter with test verifier");
        }
    }
}
