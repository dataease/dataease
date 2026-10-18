package io.dataease.share.manage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import io.dataease.chart.dao.auto.mapper.CoreChartViewRepository;
import io.dataease.dao.auto.entity.CoreChartView;
import io.dataease.dao.auto.entity.DataVisualizationInfo;
import io.dataease.permission.model.LinkIdentity;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.utils.WhitelistUtils;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoRepository;

import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/** Standalone regression: controlled repositories, no database, application startup or real tokens.
 * Compile with the current backend classes and runtime jars; run this main directly (Surefire skips tests).
 * Optional argument: a temporary JSON fixture containing componentData, charts and regionResponse.
 * It starts a loopback HTTP adapter for Playwright, using a fixed verified-identity fixture.
 * This adapter does not test token verification or the deployed gateway.
 */
public class LinkTokenValidationRegression {
    private static final LinkIdentity IDENTITY = new LinkIdentity(1L, 2L, 3L, 4L);
    private static final DataVisualizationInfo RESOURCE = new DataVisualizationInfo();
    private static List<CoreChartView> published;
    private static int assertions;

    private static void field(Object object, Class<?> type, String name, Object value) throws Exception {
        var field = type.getDeclaredField(name);
        field.setAccessible(true);
        field.set(object, value);
    }

    private static CoreChartView chart(long id, String type, String area) {
        var chart = new CoreChartView();
        chart.setId(id);
        chart.setSceneId(3L);
        chart.setType(type);
        chart.setCustomAttr("{\"map\":{\"id\":\"" + area + "\"}}");
        return chart;
    }

    private static void expect(LinkTokenValidationManage validator, String path, String method, boolean allowed) {
        boolean result = true;
        try {
            validator.checkRequestPath(path, method);
        } catch (IllegalArgumentException e) {
            result = false;
        }
        assertions++;
        if (result != allowed) throw new AssertionError(method + " " + path + " expected " + allowed);
    }

    public static void main(String[] args) throws Exception {
        var validator = new LinkTokenValidationManage();
        field(null, WhitelistUtils.class, "contextPath", "/de2api");
        field(validator, LinkTokenValidationManage.class, "visualizations", Proxy.newProxyInstance(
                DataVisualizationInfoRepository.class.getClassLoader(), new Class[]{DataVisualizationInfoRepository.class},
                (proxy, method, values) -> {
                    if (method.getName().equals("findById")) return Long.valueOf(3).equals(values[0])
                            ? Optional.of(RESOURCE) : Optional.empty();
                    throw new AssertionError("Unexpected repository call: " + method);
                }));
        field(validator, LinkTokenValidationManage.class, "charts", Proxy.newProxyInstance(
                CoreChartViewRepository.class.getClassLoader(), new Class[]{CoreChartViewRepository.class},
                (proxy, method, values) -> {
                    if (method.getName().equals("findBySceneId") && Long.valueOf(3).equals(values[0])) return published;
                    throw new AssertionError("Unscoped chart query: " + method);
                }));
        String path = "/de2api/customGeo/geoArea/custom_42";
        try {
            V3UserUtil.setLink(IDENTITY);
            RESOURCE.setComponentData("[{\"component\":\"UserView\",\"id\":\"10\"}]");
            published = List.of(chart(10, "bubble-map", "custom_42"), chart(11, "map", "custom_99"));
            expect(validator, path, "GET", true);
            expect(validator, path + "?ignored=1", "GET", true);
            expect(validator, "/customGeo/geoArea/custom_99", "GET", false);
            expect(validator, path, "POST", false);
            expect(validator, path, "DELETE", false);
            for (String suffix : List.of("list", "42", "custom_", "custom_42/", "custom_42/../custom_99",
                    "custom_%34%32", "custom_42;anything", "custom_420", "custom_99")) {
                expect(validator, "/customGeo/geoArea/" + suffix, "GET", false);
            }
            expect(validator, "/customGeo/geoSubArea/options", "GET", false);
            expect(validator, "/customGeo/geoArea/save", "POST", false);
            // A chart in a group nested in a tab is still actually present.
            RESOURCE.setComponentData("[{\"component\":\"DeTabs\",\"propValue\":[{\"componentData\":["
                    + "{\"component\":\"Group\",\"propValue\":[{\"component\":\"UserView\",\"id\":10}]}]}]}]");
            expect(validator, path, "GET", true);
            published = List.of(chart(10, "map", "custom_42"));
            expect(validator, path, "GET", true);
            // IDs mentioned in unrelated text/properties must not grant access.
            RESOURCE.setComponentData("[{\"component\":\"Text\",\"id\":10,\"propValue\":{\"id\":10}}]");
            expect(validator, path, "GET", false);
            RESOURCE.setComponentData("[{\"component\":\"UserView\",\"id\":10}]");
            published = List.of(chart(10, "bar", "custom_42"));
            expect(validator, path, "GET", false);
            // A region present only in an editor snapshot is absent from the published repository.
            published = List.of(chart(10, "map", "custom_99"));
            expect(validator, path, "GET", false);
            published.getFirst().setCustomAttr("invalid json");
            expect(validator, path, "GET", false);
            published = List.of(chart(10, "map", "custom_42"));
            RESOURCE.setComponentData("invalid json");
            expect(validator, path, "GET", false);
            RESOURCE.setComponentData("[]");
            expect(validator, path, "GET", false);
            V3UserUtil.setLink(new LinkIdentity(1L, 2L, 99L, 99L));
            expect(validator, path, "GET", false);
            V3UserUtil.clear();
            expect(validator, path, "GET", false);
        } finally {
            V3UserUtil.clear();
        }
        System.out.println("PASS " + assertions + " share-region assertions");
        if (args.length == 0) return;
        var fixture = new ObjectMapper().readTree(Files.readString(Path.of(args[0])));
        RESOURCE.setComponentData(fixture.path("componentData").asText());
        var chartList = new java.util.ArrayList<CoreChartView>();
        for (var node : fixture.path("charts")) {
            var chart = chart(node.path("id").asLong(), node.path("type").asText(), "");
            chart.setCustomAttr(node.path("customAttr").asText());
            chartList.add(chart);
        }
        published = chartList;
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            int status = 200;
            String body;
            try {
                V3UserUtil.setLink(IDENTITY);
                validator.checkRequestPath(exchange.getRequestURI().toString(), exchange.getRequestMethod());
                body = fixture.path("regionResponse").toString();
            } catch (IllegalArgumentException e) {
                status = 403;
                body = "{\"msg\":\"Region outside share scope\"}";
            } finally {
                V3UserUtil.clear();
            }
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            try (var output = exchange.getResponseBody()) { output.write(bytes); }
        });
        server.start();
        System.out.println("Isolated fixture port: " + server.getAddress().getPort());
    }
}
