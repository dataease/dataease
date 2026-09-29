package io.dataease.datasource.provider;

import io.dataease.api.ds.vo.ExcelConfiguration;
import io.dataease.api.ds.vo.ExcelSheetData;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import io.dataease.utils.LocalModelUtils;
import org.springframework.context.support.StaticMessageSource;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** Standalone regression entry; does not write to the remote share or any database. */
public class SmbFileDownloaderRegression {
    public static void main(String[] args) throws Exception {
        new Translator().setMessageSource(new StaticMessageSource());
        new LocalModelUtils().setModelValue("standalone");
        Path directory = Files.createTempDirectory("de-smb-regression-");
        Field path = ExcelUtils.class.getDeclaredField("path");
        path.setAccessible(true);
        path.set(null, directory.toString() + "/");
        try {
            ExcelConfiguration config = new ExcelConfiguration();
            config.setUserName("test");
            config.setDomain("TEST-DOMAIN");
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            config = mapper.readValue(mapper.writeValueAsString(config), ExcelConfiguration.class);
            if (!"TEST-DOMAIN".equals(config.getDomain())) throw new AssertionError("Domain was not preserved");
            if (mapper.readValue("{\"url\":\"https://example.test/data.xlsx\"}", ExcelConfiguration.class).getDomain() != null) {
                throw new AssertionError("Legacy configuration must not require a domain");
            }
            System.out.println("PASS: domain round-trip and legacy configuration");
            for (String url : List.of("smb://server/share", "smb://server/share/", "smb://server/share/../x.csv",
                    "smb://server/share/%2e%2e/x.csv", "smb://server/share/x.csv?secret=1",
                    "smb://user:secret@server/share/x.csv", "smb://server:0/share/x.csv",
                    "smb://server/share/x.csv#fragment", "smb://server/share/x%00.csv")) {
                config.setUrl(url);
                expectFailure(config, directory, "i18n_smb_invalid_address");
            }
            config.setUrl("smb://server/share/x.exe");
            expectFailure(config, directory, "i18n_unsupported_file_format");
            config.setUrl("smb://server/share/x.csv");
            config.setUserName("");
            expectFailure(config, directory, "i18n_smb_username_required");
            System.out.println("PASS: 11 invalid input cases rejected before network access");

            String url = System.getenv("SMB_TEST_URL");
            if (url == null) {
                System.out.println("SKIP: set SMB_TEST_URL, SMB_TEST_USER, SMB_TEST_PASSWORD for live download/parse tests");
                return;
            }
            config.setUrl(url);
            config.setUserName(System.getenv("SMB_TEST_USER"));
            config.setPasswd(System.getenv("SMB_TEST_PASSWORD"));
            config.setDomain(System.getenv("SMB_TEST_DOMAIN"));
            Method download = ExcelUtils.class.getDeclaredMethod("downLoadRemoteExcel", ExcelConfiguration.class);
            download.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, String> file = (Map<String, String>) download.invoke(null, config);
            Path localFile = directory.resolve(file.get("tranName"));
            if (!Files.isRegularFile(localFile) || Files.size(localFile) == 0) {
                throw new AssertionError("Download did not produce a non-empty file");
            }
            Method parse = ExcelUtils.class.getDeclaredMethod("parseExcel", String.class, InputStream.class, boolean.class, String.class);
            parse.setAccessible(true);
            try (InputStream input = Files.newInputStream(localFile)) {
                @SuppressWarnings("unchecked")
                List<ExcelSheetData> sheets = (List<ExcelSheetData>) parse.invoke(new ExcelUtils(), file.get("tranName"), input, true, file.get("fileName"));
                if (sheets.isEmpty() || sheets.stream().allMatch(s -> s.getData().isEmpty())) {
                    throw new AssertionError("No data parsed from the test file");
                }
                System.out.println("PASS: real SMB download through ExcelUtils and parse; sheets=" + sheets.size());
            }
            Files.delete(localFile);
            config.setPasswd("invalid-regression-password");
            expectFailure(config, directory, "i18n_smb_download_failed");
            config.setPasswd(System.getenv("SMB_TEST_PASSWORD"));
            config.setUrl(url.substring(0, url.lastIndexOf('/') + 1) + "nonexistent-regression-file.xlsx");
            expectFailure(config, directory, "i18n_smb_download_failed");
            try (var files = Files.list(directory)) {
                if (files.findAny().isPresent()) throw new AssertionError("Failed download leaked a temporary file");
            }
            System.out.println("PASS: wrong password and missing file fail without leaking files or error details");
        } finally {
            try (var files = Files.list(directory)) {
                for (Path file : files.toList()) Files.deleteIfExists(file);
            }
            Files.delete(directory);
        }
    }

    private static void expectFailure(ExcelConfiguration config, Path directory, String expected) {
        try {
            SmbFileDownloader.download(config, directory);
            throw new AssertionError("Expected " + expected);
        } catch (DEException e) {
            if (!expected.equals(e.getMessage())) throw new AssertionError("Unexpected error: " + e.getMessage());
        }
    }
}
