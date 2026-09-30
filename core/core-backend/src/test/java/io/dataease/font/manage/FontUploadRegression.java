package io.dataease.font.manage;

import io.dataease.exception.DEException;
import io.dataease.api.font.dto.FontDto;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import org.apache.catalina.startup.Tomcat;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.http.*;

import java.io.*;
import java.nio.file.*;
import java.net.URI;
import java.net.http.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;

/** Isolated font storage regression. No application database or existing font directory is used. */
public class FontUploadRegression {
    private static int assertions;
    private static void check(boolean value, String message) {
        assertions++;
        if (!value) throw new AssertionError(message);
    }
    private static void set(Object object, String key, Object value) throws Exception {
        if (key.equals("maxUploadSize") || key.equals("maxStorageSize")) {
            Field settings = FontManage.class.getDeclaredField("fontSettingsManage");
            settings.setAccessible(true);
            object = settings.get(object);
        }
        Field field = object.getClass().getDeclaredField(key);
        field.setAccessible(true);
        field.set(object, value);
    }
    private static FontManage manage(Path path) throws Exception {
        FontManage manage = new FontManage();
        set(manage, "path", path.toString());
        FontSettingsManage settings = new FontSettingsManage();
        var repository = java.lang.reflect.Proxy.newProxyInstance(FontUploadRegression.class.getClassLoader(),
                new Class[]{io.dataease.system.dao.auto.mapper.CoreSysSettingRepository.class},
                (proxy, method, args) -> Optional.empty());
        set(settings, "repository", repository);
        set(manage, "fontSettingsManage", settings);
        return manage;
    }
    private static long count(Path path) throws IOException {
        try (var files = Files.list(path)) {
            return files.filter(p -> !p.getFileName().toString().equals(".upload.lock")).count();
        }
    }
    private static void reject(FontManage manage, MultipartFile file, String expected) {
        try {
            manage.upload(file);
            throw new AssertionError("Upload should fail: " + expected);
        } catch (DEException e) {
            check(expected.equals(e.getMessage()), "Wrong rejection: " + e.getMessage());
        }
    }
    private static class Upload implements MultipartFile {
        final byte[] data;
        final long size;
        Upload(byte[] data, long size) { this.data = data; this.size = size; }
        public String getName() { return "file"; }
        public String getOriginalFilename() { return "sample.TTF"; }
        public String getContentType() { return "application/octet-stream"; }
        public boolean isEmpty() { return size == 0; }
        public long getSize() { return size; }
        public byte[] getBytes() { throw new AssertionError("Must not load full upload into heap"); }
        public InputStream getInputStream() throws IOException { return new ByteArrayInputStream(data); }
        public void transferTo(File file) { throw new AssertionError("Must use bounded streaming"); }
    }
    public static void main(String[] args) throws Exception {
        new io.dataease.i18n.Translator().setMessageSource(new org.springframework.context.support.StaticMessageSource());
        if (args.length != 1) throw new IllegalArgumentException("Supply a real .ttf test font");
        byte[] font = Files.readAllBytes(Path.of(args[0]));
        Path directory = Files.createTempDirectory("de-font-test-");
        FontManage manage = manage(directory);
        try {
            reject(manage, new Upload(new byte[0], 500L * 1024 * 1024), "i18n_font_upload_size_limit");
            check(count(directory) == 0, "Large upload created a file");
            reject(manage, new Upload(new byte[0], 0), "i18n_font_upload_size_limit");
            for (int i = 0; i < 20; i++) reject(manage, new Upload(new byte[100], 100), "i18n_font_upload_invalid");
            check(count(directory) == 0, "Failed fonts leaked files");
            reject(manage, new Upload(new byte[1], 1) {
                public InputStream getInputStream() throws IOException { throw new IOException("simulated read failure"); }
            }, "i18n_font_upload_failed");
            check(count(directory) == 0, "Read failure leaked file");
            set(manage, "maxUploadSize", DataSize.ofBytes(64));
            reject(manage, new Upload(new byte[65], 1), "i18n_font_upload_size_limit");
            check(count(directory) == 0, "Misreported size bypassed streaming limit");
            set(manage, "maxUploadSize", DataSize.ofMegabytes(20));
            Locale previous = Locale.getDefault();
            FontDto dto;
            try {
                Locale.setDefault(Locale.GERMANY);
                dto = manage.upload(new Upload(font, font.length));
            } finally { Locale.setDefault(previous); }
            check(dto.getName() != null && !dto.getName().isBlank(), "Font not parsed");
            check(Arrays.equals(font, Files.readAllBytes(directory.resolve(dto.getFileTransName()))), "Stored font differs");
            check(count(directory) == 1, "Successful upload leaked temporary file");
            set(manage, "maxStorageSize", DataSize.ofBytes(font.length * 2L - 1));
            reject(manage, new Upload(font, font.length), "i18n_font_storage_limit");
            check(count(directory) == 1, "Quota failure changed existing font");
            set(manage, "maxStorageSize", DataSize.ofBytes(font.length + 64L));
            reject(manage, new Upload(new byte[65], 1), "i18n_font_storage_limit");
            check(count(directory) == 1, "Stream exceeded remaining storage");
            set(manage, "maxStorageSize", DataSize.ofMegabytes(512));

            CountDownLatch entered = new CountDownLatch(1), resume = new CountDownLatch(1);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            try {
                Future<?> pending = executor.submit(() -> reject(manage, new Upload(new byte[100], 100) {
                    public InputStream getInputStream() throws IOException {
                        entered.countDown();
                        try { if (!resume.await(10, TimeUnit.SECONDS)) throw new IOException("timeout"); }
                        catch (InterruptedException e) { throw new IOException(e); }
                        return super.getInputStream();
                    }
                }, "i18n_font_upload_invalid"));
                check(entered.await(5, TimeUnit.SECONDS), "Upload did not acquire lock");
                reject(manage, new Upload(font, font.length), "i18n_font_upload_busy");
                reject(manage(directory), new Upload(font, font.length), "i18n_font_upload_busy");
                resume.countDown(); pending.get(10, TimeUnit.SECONDS);
            } finally { resume.countDown(); executor.shutdownNow(); }
            check(count(directory) == 1, "Concurrent failure leaked file");
            httpTest(manage, directory, font);
            System.out.println("PASS: " + assertions + " assertions; size, stream limit, parsing, cleanup, quota, concurrency, real HTTP multipart");
        } finally {
            try (var files = Files.walk(directory)) {
                for (Path path : files.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
            }
        }
    }
    private static void httpTest(FontManage manage, Path directory, byte[] font) throws Exception {
        Path base = Files.createTempDirectory("de-font-http-");
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(base.toString()); tomcat.setHostname("127.0.0.1"); tomcat.setPort(0);
        tomcat.getConnector().setProperty("address", "127.0.0.1");
        var context = tomcat.addContext("", base.toString());
        var servlet = Tomcat.addServlet(context, "upload", new HttpServlet() {
            protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
                try {
                    var multipart = new StandardMultipartHttpServletRequest(request);
                    manage.upload(multipart.getFile("file")); response.setStatus(200);
                } catch (DEException e) { response.setStatus(400); response.getWriter().write(e.getMessage()); }
                finally {
                    try { for (var part : request.getParts()) part.delete(); }
                    catch (Exception e) { throw new IOException(e); }
                }
            }
        });
        servlet.setMultipartConfigElement(new MultipartConfigElement(base.toString(), 500L << 20, 500L << 20, 0));
        context.addServletMappingDecoded("/typeface/uploadFile", "upload");
        try {
            tomcat.start();
            var client = HttpClient.newHttpClient();
            var uri = URI.create("http://127.0.0.1:" + tomcat.getConnector().getLocalPort() + "/typeface/uploadFile");
            for (byte[] payload : List.of(new byte[100], new byte[100], font)) {
                ByteArrayOutputStream body = new ByteArrayOutputStream();
                body.write("--test\r\nContent-Disposition: form-data; name=\"file\"; filename=\"test.ttf\"\r\nContent-Type: application/octet-stream\r\n\r\n".getBytes());
                body.write(payload); body.write("\r\n--test--\r\n".getBytes());
                var result = client.send(HttpRequest.newBuilder(uri).header("Content-Type", "multipart/form-data; boundary=test")
                        .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray())).build(), HttpResponse.BodyHandlers.ofString());
                check(result.statusCode() == (payload == font ? 200 : 400), "HTTP upload result: " + result.body());
                check(count(directory) == (payload == font ? 2 : 1), "HTTP upload left unexpected files");
            }
            var largeBody = HttpRequest.BodyPublishers.concat(
                    HttpRequest.BodyPublishers.ofString("--test\r\nContent-Disposition: form-data; name=\"file\"; filename=\"large.ttf\"\r\n\r\n"),
                    HttpRequest.BodyPublishers.ofInputStream(() -> new InputStream() {
                        private long remaining = 21L << 20;
                        public int read() { return remaining-- > 0 ? 0 : -1; }
                        public int read(byte[] bytes, int offset, int length) {
                            if (remaining <= 0) return -1;
                            int count = (int) Math.min(length, remaining);
                            Arrays.fill(bytes, offset, offset + count, (byte) 0);
                            remaining -= count;
                            return count;
                        }
                    }),
                    HttpRequest.BodyPublishers.ofString("\r\n--test--\r\n"));
            var large = client.send(HttpRequest.newBuilder(uri).header("Content-Type", "multipart/form-data; boundary=test")
                    .POST(largeBody).build(), HttpResponse.BodyHandlers.ofString());
            check(large.statusCode() == 400 && large.body().equals("i18n_font_upload_size_limit"), "Large HTTP upload accepted");
            check(count(directory) == 2, "Large HTTP upload left files");
        } finally {
            tomcat.stop(); tomcat.destroy();
            try (var files = Files.walk(base)) {
                for (Path path : files.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
            }
        }
    }
}
