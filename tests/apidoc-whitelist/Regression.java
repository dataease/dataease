import io.dataease.exception.DEException;
import io.dataease.utils.WhitelistUtils;

import java.util.List;
import java.util.jar.JarFile;

public class Regression {
    private static int count;

    private static void check(String path, boolean expected) {
        if (WhitelistUtils.match(path) != expected) {
            throw new AssertionError("Unexpected whitelist result: " + path);
        }
        count++;
    }

    public static void main(String[] args) throws Exception {
        var contextPath = WhitelistUtils.class.getDeclaredField("contextPath");
        contextPath.setAccessible(true);
        contextPath.set(null, "/deployment");
        for (String prefix : List.of("", "/de2api", "/casbi/de2api", "/oidcbi/de2api", "/deployment/de2api")) {
            for (String path : List.of("/webjars/css/app.1824bac3.css", "/webjars/js/app.c31badf5.js",
                    "/webjars/fonts/icon.woff", "/webjars/fonts/icon.woff2", "/webjars/fonts/icon.eot",
                    "/webjars/fonts/icon.ttf", "/webjars/img/loading.gif", "/webjars/img/icon.svg",
                    "/webjars/css/app.css?v=1", "/assets/app.js", "/js/app.js", "/doc.html")) {
                check(prefix + path, true);
            }
            for (String path : List.of("/v3/api-docs", "/v3/api-docs/swagger-config", "/v3/api-docs/default",
                    "/chart/getData", "/chart/getData.css", "/user/list.js", "/webjars-evil/app.js",
                    "/other/webjars/app.css", "/webjars/api.json", "/webjars/index.html", "/webjars/config.yml")) {
                check(prefix + path, false);
            }
        }
        for (String path : List.of("/webjars/../user/list.css", "/webjars/%2e%2e/user/list.css",
                "/webjars/js/app.js;other")) {
            try {
                WhitelistUtils.match(path);
                throw new AssertionError("Invalid path accepted: " + path);
            } catch (DEException expected) {
                count++;
            }
        }
        // Check every supported resource in the actual installed Knife4j UI dependency.
        try (var jar = new JarFile(args[0])) {
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith("META-INF/resources/webjars/")
                        && name.matches(".*\\.(js|css|svg|png|jpg|gif|ico|otf|ttf|woff|woff2|eot)$")) {
                    check(name.substring("META-INF/resources".length()), true);
                }
            }
        }
        System.out.println("PASS: " + count + " whitelist assertions");
    }
}
