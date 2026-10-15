package io.dataease.datasource.provider;

import com.sun.net.httpserver.HttpServer;
import io.dataease.api.ds.vo.ExcelFileData;
import io.dataease.api.ds.vo.RemoteExcelRequest;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import io.dataease.utils.LocalModelUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.context.support.StaticMessageSource;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/** Standalone regression; uses generated fixtures, loopback HTTP and optional read-only SMB access. */
public class RemoteWorkbookRegression {
    private static final long MIB = 1024L * 1024;
    private static final int ROWS = 100_000;
    private static final int COLUMNS = 20;

    public static void main(String[] args) throws Exception {
        new Translator().setMessageSource(new StaticMessageSource());
        new LocalModelUtils().setModelValue("standalone");
        Path directory = Files.createTempDirectory("de-remote-workbook-");
        Path downloads = Files.createDirectory(directory.resolve("downloads"));
        Field path = ExcelUtils.class.getDeclaredField("path");
        path.setAccessible(true);
        Object originalPath = path.get(null);
        path.set(null, downloads + "/");
        try {
            testLargeWorkbook(directory, downloads);
            testSizeBoundaries();
            testArchiveGuards(directory, downloads);
            testLiveSmb(downloads);
        } finally {
            path.set(null, originalPath);
            try (var files = Files.walk(directory)) {
                for (Path file : files.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(file);
            }
        }
    }

    private static void testLargeWorkbook(Path directory, Path downloads) throws Exception {
        Path file = directory.resolve("orders.xlsx");
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            workbook.setCompressTempFiles(true);
            var orders = workbook.createSheet("orders");
            var header = orders.createRow(0);
            for (int column = 0; column < COLUMNS; column++) header.createCell(column).setCellValue("field_" + column);
            for (int row = 0; row < ROWS; row++) {
                var cells = orders.createRow(row + 1);
                for (int column = 0; column < COLUMNS; column++) {
                    cells.createCell(column).setCellValue(row + "_" + column + "_order_description");
                }
            }
            var summary = workbook.createSheet("summary");
            summary.createRow(0).createCell(0).setCellValue("count");
            summary.createRow(1).createCell(0).setCellValue(ROWS);
            try (var output = Files.newOutputStream(file)) {
                workbook.write(output);
            }
        }
        try (ZipFile zip = new ZipFile(file.toFile())) {
            var entries = zip.stream().toList();
            long largest = entries.stream().mapToLong(ZipEntry::getSize).max().orElseThrow();
            long total = entries.stream().mapToLong(ZipEntry::getSize).sum();
            if (largest <= 32 * MIB || total <= 128 * MIB) {
                throw new AssertionError("Fixture must exceed both historical expansion limits");
            }
            System.out.println("FIXTURE: rows=" + ROWS + ", columns=" + COLUMNS
                    + ", largestEntryBytes=" + largest + ", expandedBytes=" + total);
        }
        validateArchive(file);
        withHttpFile(file, config -> {
            ExcelFileData result = new ExcelUtils().parseRemoteExcel(config);
            if (result.getSheets().size() != 2 || result.getSheets().get(0).getData().size() != 100
                    || result.getSheets().get(0).getFields().size() != COLUMNS
                    || result.getSheets().get(1).getData().size() != 1) {
                throw new AssertionError("Large workbook preview lost rows, fields or subsequent sheets");
            }
        });
        assertEmpty(downloads);
        Method fetch = ExcelUtils.class.getDeclaredMethod("fetchExcelDataList", String.class, InputStream.class);
        fetch.setAccessible(true);
        try (InputStream input = Files.newInputStream(file)) {
            @SuppressWarnings("unchecked")
            List<String[]> rows = (List<String[]>) fetch.invoke(new ExcelUtils(), "orders", input);
            if (rows.size() != ROWS || !"99999_0_order_description".equals(rows.get(ROWS - 1)[0])) {
                throw new AssertionError("Full import did not preserve all 100,000 rows");
            }
        }
        System.out.println("PASS: 100,000-row workbook, bounded preview, multiple sheets, full parser and cleanup");
    }

    private static void testSizeBoundaries() throws IOException {
        ExcelUtils.validateRemoteWorkbookSize(256 * MIB, 512 * MIB, 3 * MIB);
        expectIOException(() -> ExcelUtils.validateRemoteWorkbookSize(256 * MIB + 1, 256 * MIB + 1, 3 * MIB), "i18n_remote_zip_size");
        expectIOException(() -> ExcelUtils.validateRemoteWorkbookSize(200 * MIB, 512 * MIB + 1, 3 * MIB), "i18n_remote_zip_size");
        ExcelUtils.validateRemoteWorkbookSize(2 * MIB, 2 * MIB, 2 * MIB / 100 + 1);
        expectIOException(() -> ExcelUtils.validateRemoteWorkbookSize(2 * MIB, 2 * MIB, 2 * MIB / 100), "i18n_remote_zip_size");
        ExcelUtils.validateRemoteWorkbookSize(MIB, MIB, 1);
        expectIOException(() -> ExcelUtils.validateRemoteWorkbookSize(MIB + 1, MIB + 1, 1), "i18n_remote_zip_size");
        System.out.println("PASS: 7 entry, total, compression-ratio and grace-size boundary cases");
    }

    private static void testArchiveGuards(Path directory, Path downloads) throws Exception {
        Path bomb = directory.resolve("bomb.xlsx");
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(bomb))) {
            zip.putNextEntry(new ZipEntry("xl/worksheets/sheet1.xml"));
            byte[] zeros = new byte[8192];
            for (int block = 0; block < 256; block++) zip.write(zeros);
            zip.closeEntry();
        }
        expectIOException(() -> validateArchive(bomb), "i18n_remote_zip_size");
        Path entries = directory.resolve("entries.xlsx");
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(entries))) {
            for (int entry = 0; entry < 10_001; entry++) {
                zip.putNextEntry(new ZipEntry("part_" + entry));
                zip.closeEntry();
            }
        }
        expectIOException(() -> validateArchive(entries), "i18n_remote_zip_entries");
        Thread.currentThread().interrupt();
        try {
            expectIOException(() -> validateArchive(directory.resolve("orders.xlsx")), "i18n_remote_transfer_timeout");
        } finally {
            Thread.interrupted();
        }
        withHttpFile(bomb, config -> {
            try {
                new ExcelUtils().parseRemoteExcel(config);
                throw new AssertionError("Compressed bomb must be rejected after download");
            } catch (DEException e) {
                if (!e.getMessage().contains("i18n_remote_zip_size")) throw e;
            }
        });
        assertEmpty(downloads);
        Path csv = directory.resolve("orders.csv");
        Files.writeString(csv, "id\n1\n");
        validateArchive(csv);
        Path xls = directory.resolve("orders.xls");
        Files.writeString(xls, "not a ZIP archive");
        validateArchive(xls);
        System.out.println("PASS: ZIP bomb, entry count, interruption, failed-download cleanup and CSV/XLS dispatch");
    }

    private static void testLiveSmb(Path downloads) throws Exception {
        String url = System.getenv("SMB_TEST_URL");
        if (url == null || url.isBlank()) {
            System.out.println("SKIP: set SMB_TEST_URL, SMB_TEST_USER and SMB_TEST_PASSWORD for real SMB preview");
            return;
        }
        RemoteExcelRequest config = new RemoteExcelRequest();
        config.setUrl(url);
        config.setUserName(System.getenv("SMB_TEST_USER"));
        config.setPasswd(System.getenv("SMB_TEST_PASSWORD"));
        config.setDomain(System.getenv("SMB_TEST_DOMAIN"));
        ExcelFileData result = new ExcelUtils().parseRemoteExcel(config);
        if (result.getSheets().isEmpty() || result.getSheets().stream().allMatch(sheet -> sheet.getData().isEmpty())) {
            throw new AssertionError("Real SMB preview must contain data");
        }
        if (result.getSheets().stream().anyMatch(sheet -> sheet.getData().size() > 100)) {
            throw new AssertionError("Real SMB preview exceeded its row limit");
        }
        assertEmpty(downloads);
        System.out.println("PASS: real SMB download, workbook validation, preview and temporary-file cleanup; sheets="
                + result.getSheets().size() + ", previewRows="
                + result.getSheets().stream().mapToInt(sheet -> sheet.getData().size()).sum());
    }

    private static void validateArchive(Path file) throws IOException {
        try {
            Method validate = ExcelUtils.class.getDeclaredMethod("validateRemoteWorkbook", Path.class);
            validate.setAccessible(true);
            validate.invoke(null, file);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof IOException io) throw io;
            throw new AssertionError(e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static void withHttpFile(Path file, RemoteAction action) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/" + file.getFileName(), exchange -> {
            try (exchange) {
                exchange.sendResponseHeaders(200, Files.size(file));
                Files.copy(file, exchange.getResponseBody());
            }
        });
        server.start();
        try {
            RemoteExcelRequest config = new RemoteExcelRequest();
            config.setUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/" + file.getFileName());
            action.run(config);
        } finally {
            server.stop(0);
        }
    }

    private static void assertEmpty(Path directory) throws IOException {
        try (var files = Files.list(directory)) {
            if (files.findAny().isPresent()) throw new AssertionError("Remote operation leaked a temporary file");
        }
    }

    private static void expectIOException(IoAction action, String message) throws IOException {
        try {
            action.run();
            throw new AssertionError("Expected " + message);
        } catch (IOException e) {
            if (!message.equals(e.getMessage())) throw new AssertionError("Unexpected guard error", e);
        }
    }

    private interface IoAction {
        void run() throws IOException;
    }

    private interface RemoteAction {
        void run(RemoteExcelRequest config) throws Exception;
    }
}
