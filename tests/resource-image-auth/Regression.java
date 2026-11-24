import io.dataease.utils.WhitelistUtils;

import java.util.List;

public class Regression {
    public static void main(String[] args) throws Exception {
        var field = WhitelistUtils.class.getDeclaredField("contextPath");
        field.setAccessible(true);
        field.set(null, "/tenant");
        int count = 0;
        for (String prefix : List.of("", "/de2api", "/tenant/de2api", "/casbi/de2api", "/oidcbi/de2api")) {
            for (String file : List.of("background.png", "background.jpg", "background.svg", "background.gif", "background.jpeg")) {
                for (String query : List.of("", "?v=1")) {
                    String path = prefix + "/static-resource/" + file + query;
                    if (WhitelistUtils.match(path)) {
                        throw new AssertionError("Image bypassed authentication: " + path);
                    }
                    count++;
                }
            }
        }
        for (String path : List.of("/assets/app.js", "/assets/app.css", "/embedded/initIframe", "/login/localLogin")) {
            if (!WhitelistUtils.match(path)) {
                throw new AssertionError("Public path changed: " + path);
            }
            count++;
        }
        System.out.println("PASS: " + count + " image authentication boundary assertions");
    }
}
