package io.dataease.regression;

import io.dataease.datasource.provider.ExcelUtils;
import io.dataease.api.ds.vo.RemoteExcelRequest;
import io.dataease.exception.*;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.util.concurrent.Executors;

/** Real loopback HTTP and the production remote Excel parser; all files are temporary. */
public class RemoteFileRegression {
    public static void main(String[] args) throws Exception {
        var messages = new io.dataease.i18n.DeReloadableResourceBundleMessageSource();
        messages.setBasename("classpath:i18n/core");
        new io.dataease.i18n.Translator().setMessageSource(messages);
        var model = io.dataease.utils.LocalModelUtils.class.getDeclaredField("modelValue");
        model.setAccessible(true); model.set(null, "standalone");
        Path directory = Files.createTempDirectory("error-contract-files-");
        var path = ExcelUtils.class.getDeclaredField("path"); path.setAccessible(true);
        Object previous = path.get(null); path.set(null, directory.toString() + "/");
        HttpServer http = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            http.setExecutor(executor);
            http.createContext("/", exchange -> {
                try {
                    String name = exchange.getRequestURI().getPath();
                    if (name.contains("slow")) Thread.sleep(1500);
                    byte[] bytes = "name,value\nhello,1\n".getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(name.contains("unavailable") ? 503 : 200, bytes.length);
                    exchange.getResponseBody().write(bytes);
                } catch (Exception ignored) { }
                finally { exchange.close(); }
            });
            http.start();
            String base = "http://127.0.0.1:" + http.getAddress().getPort();
            RemoteExcelRequest request = new RemoteExcelRequest();
            request.setUrl(base + "/valid.csv"); request.setTransferTimeoutSeconds(1);
            var result = new ExcelUtils().parseRemoteExcel(request);
            if (result.getSheets().isEmpty() || result.getSheets().get(0).getFields().isEmpty()) throw new AssertionError("valid file failed");
            for (var entry : java.util.Map.of("/slow.csv", ErrorCategory.UPSTREAM_TIMEOUT,
                    "/unavailable.csv", ErrorCategory.UPSTREAM, "/unsupported.bin", ErrorCategory.VALIDATION,
                    "/invalid.xlsx", ErrorCategory.VALIDATION).entrySet()) {
                request.setUrl(base + entry.getKey());
                try { new ExcelUtils().parseRemoteExcel(request); throw new AssertionError("failure accepted"); }
                catch (DEException failure) {
                    if (failure.getCategory() != entry.getValue()) throw new AssertionError(entry.getKey() + ": " + failure.getCategory(), failure);
                    if (failure.getMessage().contains("127.0.0.1")) throw new AssertionError("remote details leaked");
                }
            }
            try (var files = Files.list(directory)) {
                if (files.findAny().isPresent()) throw new AssertionError("partial file not cleaned");
            }
            System.out.println("PASS remote CSV success, upstream failure/timeout, invalid format and cleanup");
        } finally {
            http.stop(0); path.set(null, previous);
            try (var files = Files.list(directory)) { for (Path file : files.toList()) Files.deleteIfExists(file); }
            Files.deleteIfExists(directory);
        }
    }
}
