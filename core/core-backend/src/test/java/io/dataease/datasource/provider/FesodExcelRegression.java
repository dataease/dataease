package io.dataease.datasource.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sun.net.httpserver.HttpServer;
import io.dataease.api.ds.vo.ExcelFileData;
import io.dataease.api.ds.vo.ExcelSheetData;
import io.dataease.api.ds.vo.RemoteExcelRequest;
import io.dataease.dao.auto.entity.CoreDatasource;
import io.dataease.extensions.datasource.dto.DatasourceDTO;
import io.dataease.utils.JsonUtil;
import io.dataease.excel.ExcelCell;
import io.dataease.excel.ExcelHeaderComment;
import io.dataease.excel.ExcelIO;
import io.dataease.excel.ExcelReadHandler;
import io.dataease.excel.ExcelReadOptions;
import io.dataease.excel.ExcelReadSession;
import io.dataease.excel.ExcelReadStopException;
import io.dataease.excel.ExcelSheetInfo;
import io.dataease.excel.ExcelWriteOptions;
import io.dataease.i18n.Translator;
import io.dataease.utils.LocalModelUtils;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fesod Excel adapter and ExcelUtils regression. Standalone entry; generates synthetic
 * XLS/XLSX/CSV fixtures and asserts data, styles, comments, preview stop, remote dispatch
 * and resource cleanup. Run with a classpath prepared as described in
 * src/test/scripts/README-fesod-excel.md.
 */
public class FesodExcelRegression {

    private static final int PREVIEW_ROWS = 100;
    private static final int FULL_ROWS = 120;

    public static void main(String[] args) throws Exception {
        new Translator().setMessageSource(new StaticMessageSource());
        new LocalModelUtils().setModelValue("standalone");
        Path samplesDir = parseSamplesDir(args);
        Path manifestFile = parseManifestFile(args);
        Path directory = Files.createTempDirectory("de-fesod-excel-");
        Path downloads = Files.createDirectory(directory.resolve("downloads"));
        Field path = ExcelUtils.class.getDeclaredField("path");
        path.setAccessible(true);
        Object originalPath = path.get(null);
        path.set(null, downloads + "/");
        try {
            testDataCorrectnessXlsx(directory);
            testDate1904Workbook(directory);
            testMultiSheetAndXls(directory);
            testCsvBusinessFlow(directory);
            testPreviewStopAndFullParse(directory);
            testWriteTemplate(directory);
            testWriteModelRoundTripAndStyles(directory);
            testEarlyStopAndConsecutiveReads(directory);
            testParseFailure(directory);
            testWriteFailure();
            testRemoteXlsx(directory, downloads);
            testExcelSaveAndParse(directory);
            testMergeSheets();
            if (samplesDir != null) {
                testExternalSamples(samplesDir, manifestFile);
            }
            System.out.println("RESULT: all Fesod Excel regression checks passed");
        } finally {
            path.set(null, originalPath);
            try (var files = Files.walk(directory)) {
                for (Path file : files.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(file);
            }
        }
    }

    private static Path parseSamplesDir(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if ("--samples".equals(args[i]) && i + 1 < args.length) {
                return Path.of(args[i + 1]).toAbsolutePath().normalize();
            }
        }
        String env = System.getenv("FESOD_SAMPLE_DIR");
        return env == null || env.isBlank() ? null : Path.of(env).toAbsolutePath().normalize();
    }

    private static Path parseManifestFile(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if ("--manifest".equals(args[i]) && i + 1 < args.length) {
                return Path.of(args[i + 1]).toAbsolutePath().normalize();
            }
        }
        String env = System.getenv("FESOD_SAMPLE_MANIFEST");
        return env == null || env.isBlank() ? null : Path.of(env).toAbsolutePath().normalize();
    }

    // ------------------------------------------------------------------
    // fixtures
    // ------------------------------------------------------------------

    private static Path writeDataCorrectnessXlsx(Path directory) throws IOException {
        Path file = directory.resolve("data-correctness.xlsx");
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("data");
            Row header = sheet.createRow(0);
            String[] headers = {"text", "empty_col", "long_text", "high_precision", "date_1900",
                    "percent", "bool", "error", "special"};
            for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);

            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("hello");
            // 1: empty column intentionally left blank
            Cell longText = row.createCell(2);
            longText.setCellValue("12345678901234567890"); // must stay text, never scientific notation
            Cell highPrecision = row.createCell(3);
            highPrecision.setCellValue(1234567890.1234567d);
            Cell date1900 = row.createCell(4);
            date1900.setCellValue(45292d);
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd"));
            date1900.setCellStyle(dateStyle);
            Cell percent = row.createCell(5);
            percent.setCellValue(0.25d);
            CellStyle percentStyle = workbook.createCellStyle();
            percentStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00%"));
            percent.setCellStyle(percentStyle);
            row.createCell(6).setCellValue(true);
            Cell error = row.createCell(7);
            error.setCellErrorValue(FormulaError.DIV0.getCode());
            row.createCell(8).setCellValue("中文,逗号;分号\"引号\n换行");
            try (var out = Files.newOutputStream(file)) {
                workbook.write(out);
            }
        }
        return file;
    }

    /**
     * A workbook whose {@code workbookPr/date1904} flag is set must use the 1904 epoch.
     * Serial 45292 is 2024-01-02 in the 1900 system but 2028-01-02 in the 1904 system.
     */
    private static Path writeDate1904Xlsx(Path directory) throws IOException {
        Path file = directory.resolve("date-1904.xlsx");
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbook ctWorkbook = workbook.getCTWorkbook();
            org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbookPr workbookPr =
                    ctWorkbook.isSetWorkbookPr() ? ctWorkbook.getWorkbookPr() : ctWorkbook.addNewWorkbookPr();
            workbookPr.setDate1904(true);
            Sheet sheet = workbook.createSheet("dates");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("date");
            Row row = sheet.createRow(1);
            Cell date1904 = row.createCell(0);
            date1904.setCellValue(45292d);
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd"));
            date1904.setCellStyle(dateStyle);
            try (var out = Files.newOutputStream(file)) {
                workbook.write(out);
            }
        }
        return file;
    }

    private static Path writeMultiSheetXlsx(Path directory) throws IOException {
        Path file = directory.resolve("multi-sheet.xlsx");
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            for (int sheetIndex = 0; sheetIndex < 3; sheetIndex++) {
                Sheet sheet = workbook.createSheet("sheet_" + sheetIndex);
                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("id");
                header.createCell(1).setCellValue("name");
                for (int rowIndex = 0; rowIndex < 5; rowIndex++) {
                    Row row = sheet.createRow(rowIndex + 1);
                    row.createCell(0).setCellValue(rowIndex + 1);
                    row.createCell(1).setCellValue("name_" + sheetIndex + "_" + rowIndex);
                }
            }
            try (var out = Files.newOutputStream(file)) {
                workbook.write(out);
            }
        }
        return file;
    }

    private static Path writeXls(Path directory) throws IOException {
        Path file = directory.resolve("legacy.xls");
        try (HSSFWorkbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("legacy");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("id");
            header.createCell(1).setCellValue("value");
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue(7);
            Cell dateCell = row.createCell(1);
            dateCell.setCellValue(DateUtil.getJavaDate(45292d));
            CellStyle style = workbook.createCellStyle();
            style.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd"));
            dateCell.setCellStyle(style);
            try (var out = Files.newOutputStream(file)) {
                workbook.write(out);
            }
        }
        return file;
    }

    private static Path writeCsv(Path directory) throws IOException {
        Path file = directory.resolve("business.csv");
        StringBuilder csv = new StringBuilder("id,name,note\n");
        for (int i = 0; i < FULL_ROWS; i++) {
            csv.append(i + 1).append(",\"name_").append(i).append("\",\"备注,含逗号\"\n");
        }
        Files.writeString(file, csv.toString(), StandardCharsets.UTF_8);
        return file;
    }

    private static Path writeManyRowsXlsx(Path directory) throws IOException {
        Path file = directory.resolve("preview-rows.xlsx");
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("data");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("long_col");
            header.createCell(1).setCellValue("double_col");
            header.createCell(2).setCellValue("text_col");
            for (int i = 0; i < FULL_ROWS; i++) {
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(0.5d + i);
                row.createCell(2).setCellValue("text_" + i);
            }
            try (var out = Files.newOutputStream(file)) {
                workbook.write(out);
            }
        }
        return file;
    }

    private static Path writeRemoteXlsx(Path directory) throws IOException {
        Path file = directory.resolve("remote.xlsx");
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("remote");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("id");
            for (int i = 0; i < 5; i++) {
                sheet.createRow(i + 1).createCell(0).setCellValue(i + 1);
            }
            try (var out = Files.newOutputStream(file)) {
                workbook.write(out);
            }
        }
        return file;
    }

    // ------------------------------------------------------------------
    // checks
    // ------------------------------------------------------------------

    private static void testDataCorrectnessXlsx(Path directory) throws Exception {
        Path file = writeDataCorrectnessXlsx(directory);
        CollectingHandler handler = new CollectingHandler();
        ExcelIO.read(Files.newInputStream(file), new ExcelReadOptions(), handler);
        check(handler.header.size() == 9, "header column count");
        check(handler.rows.size() == 1, "data row count");
        Map<Integer, ExcelCell> row = handler.rows.get(0);
        check("hello".equals(row.get(0).getStringValue()), "plain text cell");
        check(!row.containsKey(1) || row.get(1).getStringValue() == null || row.get(1).getStringValue().isEmpty(),
                "intermediate empty column");
        check("12345678901234567890".equals(row.get(2).getStringValue()), "long numeric text preserved");
        check(row.get(3).getStringValue() != null && row.get(3).getStringValue().contains("1234567890"),
                "high precision numeric value");
        check(row.get(4).getStringValue() != null && row.get(4).getStringValue().contains("2024"),
                "1900 date system");
        check(row.get(5).getStringValue() != null, "percent cell");
        check(row.get(6).getStringValue() != null && row.get(6).getStringValue().contains("true"),
                "boolean cell");
        check(row.get(8).getStringValue() != null && row.get(8).getStringValue().contains("中文"),
                "special characters");
        System.out.println("PASS: data correctness XLSX (empty columns, long text, precision, 1900 dates, percent, bool, special chars)");
    }

    private static void testDate1904Workbook(Path directory) throws Exception {
        Path file = writeDate1904Xlsx(directory);
        CollectingHandler handler = new CollectingHandler();
        ExcelIO.read(Files.newInputStream(file), new ExcelReadOptions(), handler);
        check(handler.header.size() == 1, "1904 workbook header count");
        check(handler.rows.size() == 1, "1904 workbook row count");
        String value = handler.rows.get(0).get(0).getStringValue();
        check(value != null && value.contains("2028"),
                "1904 date system uses workbook 1904 epoch, expected 2028 but got: " + value);
        System.out.println("PASS: 1904 date system workbook (workbookPr date1904=true)");
    }

    private static void testMultiSheetAndXls(Path directory) throws Exception {
        Path xlsx = writeMultiSheetXlsx(directory);
        try (ExcelReadSession session = ExcelIO.openRead(Files.newInputStream(xlsx), new ExcelReadOptions())) {
            check(session.getSheets().size() == 3, "xlsx multi-sheet count");
            List<ExcelSheetInfo> sheets = session.getSheets();
            for (int i = 0; i < sheets.size(); i++) {
                CollectingHandler handler = new CollectingHandler();
                session.read(sheets.get(i), handler);
                check(handler.rows.size() == 5, "xlsx sheet " + i + " row count");
            }
        }
        Path xls = writeXls(directory);
        CollectingHandler handler = new CollectingHandler();
        ExcelIO.read(Files.newInputStream(xls), new ExcelReadOptions(), handler);
        check(handler.header.size() == 2, "xls header count");
        check(handler.rows.size() == 1, "xls row count");
        check(handler.rows.get(0).get(1).getStringValue() != null, "xls date value");
        System.out.println("PASS: multi-sheet XLSX and legacy XLS");
    }

    private static void testCsvBusinessFlow(Path directory) throws Exception {
        Path csv = writeCsv(directory);
        List<ExcelSheetData> preview = parseExcel("business.csv", Files.newInputStream(csv), true, "business.csv");
        check(preview.size() == 1, "csv preview sheet count");
        check(preview.get(0).getFields().size() == 3, "csv header fields");
        check(preview.get(0).getData().size() == PREVIEW_ROWS, "csv preview row limit");
        check(preview.get(0).getData().get(0)[1].equals("name_0"), "csv first preview row");
        List<ExcelSheetData> full = parseExcel("business.csv", Files.newInputStream(csv), false, "business.csv");
        check(full.get(0).getData().size() == FULL_ROWS, "csv full row count");
        check(full.get(0).getData().get(FULL_ROWS - 1)[2].contains("备注,含逗号"), "csv quoted comma preserved");
        System.out.println("PASS: CSV business flow (header, preview limit, full parse, quoted commas)");
    }

    private static void testPreviewStopAndFullParse(Path directory) throws Exception {
        Path xlsx = writeManyRowsXlsx(directory);
        List<ExcelSheetData> preview = parseExcel("preview-rows.xlsx", Files.newInputStream(xlsx), true, "preview-rows.xlsx");
        check(preview.size() == 1, "preview sheet count");
        check(preview.get(0).getData().size() == PREVIEW_ROWS, "preview stopped at 100 rows");
        check(preview.get(0).getData().get(0)[0].equals("1"), "preview first row");
        check("LONG".equals(preview.get(0).getFields().get(0).getFieldType()), "preview inferred LONG");
        check("DOUBLE".equals(preview.get(0).getFields().get(1).getFieldType()), "preview inferred DOUBLE");
        check("TEXT".equals(preview.get(0).getFields().get(2).getFieldType()), "preview inferred TEXT");
        List<ExcelSheetData> full = parseExcel("preview-rows.xlsx", Files.newInputStream(xlsx), false, "preview-rows.xlsx");
        check(full.get(0).getData().size() == FULL_ROWS, "full parse row count");
        check(full.get(0).getData().get(FULL_ROWS - 1)[2].equals("text_" + (FULL_ROWS - 1)), "full parse last row");
        System.out.println("PASS: 100-row preview early stop, type inference and full parse");
    }

    private static void testExcelSaveAndParse(Path directory) throws Exception {
        Path xlsx = writeManyRowsXlsx(directory);
        byte[] bytes = Files.readAllBytes(xlsx);
        SyntheticMultipartFile multipartFile = new SyntheticMultipartFile("user-import.xlsx", bytes);
        ExcelFileData result = new ExcelUtils().excelSaveAndParse(multipartFile, "12345");
        check(result.getSheets().size() == 1, "user import sheet count");
        check(result.getSheets().get(0).getFields().size() == 3, "user import field count");
        check(result.getSheets().get(0).getData().size() == PREVIEW_ROWS, "user import preview rows");
        check(result.getPath() != null && Files.exists(Path.of(result.getPath())), "user import persisted file exists");
        System.out.println("PASS: user import save and parse through ExcelUtils.excelSaveAndParse");
    }

    private static void testWriteTemplate(Path directory) throws Exception {
        ExcelWriteOptions options = new ExcelWriteOptions()
                .setSheetName("template")
                .setHead(List.of(List.of("i18n_id"), List.of("i18n_name")))
                .setHeaderComments(List.of(new ExcelHeaderComment(0, "template id comment")));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelIO.writeTemplate(out, options);
        out.close();
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()))) {
            Sheet sheet = workbook.getSheet("template");
            check(sheet != null, "template sheet exists");
            Row header = sheet.getRow(0);
            check(header.getCell(0).getStringCellValue().equals("i18n_id"), "template first header");
            check(header.getCell(1).getStringCellValue().equals("i18n_name"), "template second header");
        }
        System.out.println("PASS: template write, header row preserved");
    }

    private static void testWriteModelRoundTripAndStyles(Path directory) throws Exception {
        List<SyntheticRecord> records = new ArrayList<>();
        records.add(new SyntheticRecord("alpha", 1234567890123456789L, 1.25d, "2024-01-02"));
        records.add(new SyntheticRecord("beta", 9876543210L, 2.5d, "2024-02-03"));
        ExcelWriteOptions options = new ExcelWriteOptions()
                .setSheetName("synthetic")
                .setHeaderComments(List.of(new ExcelHeaderComment(1, "long number comment")))
                .setErrMsgList(List.of(Map.of(0, "name error")));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelIO.writeModel(out, SyntheticRecord.class, records, options);
        out.close();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()))) {
            Sheet sheet = workbook.getSheet("synthetic");
            check(sheet != null, "written sheet exists");
            check(sheet.getPhysicalNumberOfRows() == 3, "written row count includes header");
            Row header = sheet.getRow(0);
            check(header.getCell(0).getStringCellValue().equals("i18n_name"), "header text written");
            check(header.getCell(1).getStringCellValue().equals("i18n_long_number"), "header text written with i18n key passthrough");
            Comment headerComment = header.getCell(1).getCellComment();
            check(headerComment != null && headerComment.getString().getString().contains("long number comment"),
                    "header comment written");
            Row firstData = sheet.getRow(1);
            check(firstData.getCell(0).getStringCellValue().equals("alpha"), "first data row text");
            check(firstData.getCell(1).getNumericCellValue() == 1234567890123456789d, "long number written");
            Cell errorCell = firstData.getCell(0);
            check(errorCell.getCellComment() != null && errorCell.getCellComment().getString().getString().contains("name error"),
                    "error postil comment written");
            check(errorCell.getCellStyle().getFillForegroundColor() == IndexedColors.RED.getIndex(),
                    "error postil red fill written");
            check(errorCell.getCellStyle().getFillPattern() == FillPatternType.SOLID_FOREGROUND,
                    "error postil fill pattern written");
        }

        List<SyntheticRecord> readBack = new ArrayList<>();
        ExcelIO.readModel(new ByteArrayInputStream(out.toByteArray()), SyntheticRecord.class,
                new ExcelReadOptions().setHeadRowNumber(1),
                (rowIndex, data) -> readBack.add(data));
        check(readBack.size() == 2, "model read-back row count");
        check("alpha".equals(readBack.get(0).getName()), "model read-back first value");
        check(readBack.get(1).getLongNumber() == 9876543210L, "model read-back long value");
        System.out.println("PASS: write model, re-read counts/types, header comments, error postil styles");
    }

    private static void testEarlyStopAndConsecutiveReads(Path directory) throws Exception {
        Path first = writeMultiSheetXlsx(directory);
        Path second = writeXls(directory);
        Path third = writeManyRowsXlsx(directory);
        TrackingInputStream firstStream = new TrackingInputStream(Files.newInputStream(first));
        CollectingHandler firstHandler = new CollectingHandler();
        ExcelIO.read(firstStream, new ExcelReadOptions(), firstHandler);
        check(firstStream.closed, "first workbook stream closed");
        check(firstHandler.header.size() == 2, "first workbook header after stream close");
        CollectingHandler secondHandler = new CollectingHandler();
        TrackingInputStream secondStream = new TrackingInputStream(Files.newInputStream(second));
        ExcelIO.read(secondStream, new ExcelReadOptions(), secondHandler);
        check(secondStream.closed, "second workbook stream closed");
        check(secondHandler.header.get(0).getStringValue().equals("id"), "second workbook header");
        CollectingHandler thirdHandler = new CollectingHandler();
        TrackingInputStream thirdStream = new TrackingInputStream(Files.newInputStream(third));
        ExcelIO.read(thirdStream, new ExcelReadOptions(), thirdHandler);
        check(thirdStream.closed, "third workbook stream closed");
        check(thirdHandler.header.size() == 3, "third workbook header");

        TrackingInputStream stopStream = new TrackingInputStream(Files.newInputStream(third));
        CountingStopHandler stopHandler = new CountingStopHandler();
        ExcelIO.read(stopStream, new ExcelReadOptions(), stopHandler);
        check(stopHandler.rows == 1, "early stop after first row");
        check(stopStream.closed, "early-stop stream closed");
        System.out.println("PASS: stream release, same-thread consecutive workbooks, early stop");
    }

    private static void testParseFailure(Path directory) throws Exception {
        Path invalid = directory.resolve("invalid.xlsx");
        Files.writeString(invalid, "not an xlsx");
        try {
            ExcelIO.read(Files.newInputStream(invalid), new ExcelReadOptions(), new CollectingHandler());
            throw new AssertionError("invalid workbook must fail");
        } catch (RuntimeException | IOException expected) {
            // Fesod surfaces parse failures as RuntimeException; IO wrapper may also fail.
        }
        try {
            parseExcel("invalid.xlsx", Files.newInputStream(invalid), true, "invalid.xlsx");
            throw new AssertionError("invalid workbook must fail in ExcelUtils");
        } catch (Exception expected) {
            // DEException or wrapped RuntimeException expected.
        }
        System.out.println("PASS: parse failure surfaces instead of returning empty success");
    }

    private static void testWriteFailure() {
        try {
            ExcelIO.writeModel(new FailingOutputStream(), SyntheticRecord.class,
                    List.of(new SyntheticRecord("alpha", 1L, 1.0d, "2024-01-01")), new ExcelWriteOptions());
            throw new AssertionError("write failure must surface as RuntimeException");
        } catch (RuntimeException expected) {
            // expected
        }
        System.out.println("PASS: write failure surfaces instead of silent success");
    }

    private static void testRemoteXlsx(Path directory, Path downloads) throws Exception {
        Path file = writeRemoteXlsx(directory);
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/" + file.getFileName(), exchange -> {
            try (exchange) {
                exchange.sendResponseHeaders(200, Files.size(file));
                Files.copy(file, exchange.getResponseBody());
            }
        });
        server.start();
        try {
            RemoteExcelRequest request = new RemoteExcelRequest();
            request.setUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/" + file.getFileName());
            ExcelFileData result = new ExcelUtils().parseRemoteExcel(request);
            check(result.getSheets().size() == 1, "remote sheet count");
            check(result.getSheets().get(0).getData().size() == 5, "remote preview rows");
        } finally {
            server.stop(0);
        }
        check(isEmpty(downloads), "remote temporary files cleaned");
        System.out.println("PASS: loopback remote XLSX dispatch, preview and temp-file cleanup");
    }

    private static void testMergeSheets() {
        ExcelSheetData oldSheet = new ExcelSheetData();
        oldSheet.setDeTableName("old_table");
        ExcelSheetData newSheet = new ExcelSheetData();
        newSheet.setDeTableName("new_table");
        CoreDatasource request = new CoreDatasource();
        request.setType("Excel");
        request.setConfiguration(JsonUtil.toJSONString(List.of(newSheet)).toString());
        DatasourceDTO source = new DatasourceDTO();
        source.setConfiguration(JsonUtil.toJSONString(List.of(oldSheet)).toString());
        ExcelUtils.mergeSheets(request, source);
        List<ExcelSheetData> merged = JsonUtil.parseList(request.getConfiguration(),
                new TypeReference<List<ExcelSheetData>>() {});
        check(merged.size() == 2, "mergeSheets append keeps old and new sheets");
        check(merged.stream().anyMatch(sheet -> "old_table".equals(sheet.getDeTableName())), "mergeSheets append contains old sheet");

        ExcelSheetData replacement = new ExcelSheetData();
        replacement.setDeTableName("new_table");
        request.setConfiguration(JsonUtil.toJSONString(List.of(replacement)).toString());
        ExcelUtils.mergeSheets(request, source);
        List<ExcelSheetData> replaced = JsonUtil.parseList(request.getConfiguration(),
                new TypeReference<List<ExcelSheetData>>() {});
        check(replaced.size() == 2, "mergeSheets replace keeps two sheets");
        check(replaced.stream().filter(sheet -> "new_table".equals(sheet.getDeTableName())).count() == 1,
                "mergeSheets replace deduplicates same deTableName");
        System.out.println("PASS: append/replace sheet merge semantics through ExcelUtils.mergeSheets");
    }

    // ------------------------------------------------------------------
    // external sample acceptance (e.g. the 31 review attachments)
    // ------------------------------------------------------------------

    private static void testExternalSamples(Path samplesDir, Path manifestFile) throws Exception {
        if (!Files.isDirectory(samplesDir)) {
            throw new AssertionError("sample directory does not exist: " + samplesDir);
        }
        Path effectiveManifest = manifestFile != null && Files.exists(manifestFile)
                ? manifestFile
                : samplesDir.resolve("samples-manifest.json");
        if (manifestFile != null && !Files.exists(manifestFile)) {
            throw new AssertionError("manifest file does not exist: " + manifestFile);
        }
        if (Files.exists(effectiveManifest)) {
            List<SampleExpectation> expectations = JsonUtil.parseList(Files.readString(effectiveManifest),
                    new TypeReference<List<SampleExpectation>>() {});
            if (expectations == null || expectations.isEmpty()) {
                throw new AssertionError("manifest is empty: " + effectiveManifest);
            }
            runManifestSamples(samplesDir, expectations);
        } else {
            runGenericSamples(samplesDir);
        }
    }

    private static void runManifestSamples(Path samplesDir, List<SampleExpectation> expectations) throws Exception {
        int accepted = 0;
        int rejected = 0;
        for (SampleExpectation expectation : expectations) {
            Path sample = samplesDir.resolve(expectation.file).normalize();
            if (!sample.startsWith(samplesDir)) {
                throw new AssertionError("sample path escapes sample directory: " + expectation.file);
            }
            if (!Files.isRegularFile(sample)) {
                throw new AssertionError("sample file missing: " + sample);
            }
            if (expectation.sha256 != null) {
                String actual = sha256(sample);
                if (!expectation.sha256.equalsIgnoreCase(actual)) {
                    throw new AssertionError("sample sha256 mismatch [" + expectation.file + "]: expected "
                            + expectation.sha256 + " but was " + actual);
                }
            }
            try {
                if ("accept".equalsIgnoreCase(expectation.expect)) {
                    runAcceptSample(sample, expectation);
                    accepted++;
                    System.out.println("SAMPLE ACCEPT OK: " + expectation.file + " -> " + sample.getFileName());
                } else {
                    runRejectSample(sample, expectation);
                    rejected++;
                    System.out.println("SAMPLE REJECT OK: " + expectation.file + " -> " + sample.getFileName());
                }
            } catch (AssertionError | Exception e) {
                throw new AssertionError("sample mismatch [" + expectation.file + "]: " + e.getMessage(), e);
            }
        }
        System.out.printf("SAMPLE SUMMARY: %d manifest entries, %d accepted, %d rejected%n",
                expectations.size(), accepted, rejected);
    }

    private static void runGenericSamples(Path samplesDir) throws Exception {
        List<Path> samples = listSampleFiles(samplesDir);
        int accepted = 0;
        int rejected = 0;
        for (Path sample : samples) {
            try {
                runAcceptSample(sample, null);
                accepted++;
                System.out.println("SAMPLE ACCEPT: " + sample.getFileName());
            } catch (Exception e) {
                rejected++;
                System.out.println("SAMPLE REJECT: " + sample.getFileName() + " -> " + e.getMessage());
            }
        }
        System.out.printf("SAMPLE SUMMARY: %d files, %d accepted, %d rejected (no manifest; " +
                "provide samples-manifest.json for deterministic expectations)%n", samples.size(), accepted, rejected);
    }

    private static void runAcceptSample(Path sample, SampleExpectation expectation) throws Exception {
        String filename = sample.getFileName().toString();
        byte[] bytes = Files.readAllBytes(sample);
        ExcelFileData data = new ExcelUtils().excelSaveAndParse(new SyntheticMultipartFile(filename, bytes), "12345");
        if (data == null || data.getSheets() == null || data.getSheets().isEmpty()) {
            throw new AssertionError("no sheet parsed from " + filename);
        }
        ExcelSheetData first = data.getSheets().get(0);
        if (expectation != null) {
            if (expectation.sheets != null) {
                check(data.getSheets().size() == expectation.sheets, filename + " sheet count");
            }
            if (expectation.fields != null) {
                check(first.getFields().size() == expectation.fields, filename + " field count");
            }
            if (expectation.previewRows != null) {
                check(first.getData().size() == expectation.previewRows, filename + " preview row count");
            }
            if (expectation.dataRows != null) {
                List<ExcelSheetData> full = parseExcel(filename, new ByteArrayInputStream(bytes), false, filename);
                check(full.get(0).getData().size() == expectation.dataRows, filename + " full data row count");
            }
        }
        String persisted = first.getPath();
        check(persisted != null && Files.isReadable(Path.of(persisted)), filename + " persisted import file readable");
        if (expectation != null && (expectation.verifyPersisted == null || expectation.verifyPersisted)) {
            verifyPersistedFile(filename, Path.of(persisted), first.getFields().size(), first.getData().size());
        }
    }

    private static void verifyPersistedFile(String filename, Path persisted, int fields, int previewRows) throws Exception {
        String suffix = filename.toLowerCase();
        if (suffix.endsWith(".csv")) {
            List<ExcelSheetData> full = parseExcel(filename, Files.newInputStream(persisted), false, filename);
            check(full.get(0).getFields().size() == fields, filename + " persisted csv field count");
            check(full.get(0).getData().size() >= previewRows, filename + " persisted csv row count");
            return;
        }
        CollectingHandler handler = new CollectingHandler();
        ExcelIO.read(Files.newInputStream(persisted), new ExcelReadOptions(), handler);
        check(handler.header.size() == fields, filename + " persisted workbook field count");
        check(handler.rows.size() >= previewRows, filename + " persisted workbook row count");
    }

    private static void runRejectSample(Path sample, SampleExpectation expectation) throws Exception {
        String filename = sample.getFileName().toString();
        byte[] bytes = Files.readAllBytes(sample);
        try {
            new ExcelUtils().excelSaveAndParse(new SyntheticMultipartFile(filename, bytes), "12345");
            throw new AssertionError(filename + " was expected to be rejected but import succeeded");
        } catch (AssertionError expected) {
            throw expected;
        } catch (Exception expected) {
            if (expectation != null && expectation.errorContains != null
                    && !containsIgnoreCase(String.valueOf(expected.getMessage()), expectation.errorContains)) {
                throw new AssertionError(filename + " rejection reason expected to contain ["
                        + expectation.errorContains + "] but was: " + expected.getMessage());
            }
        }
    }

    private static List<Path> listSampleFiles(Path samplesDir) throws IOException {
        List<String> suffixes = List.of(".xlsx", ".xls", ".csv");
        try (var files = Files.walk(samplesDir)) {
            return files.filter(path -> Files.isRegularFile(path))
                    .filter(path -> suffixes.stream().anyMatch(suffix ->
                            path.getFileName().toString().toLowerCase().endsWith(suffix)))
                    .sorted()
                    .toList();
        }
    }

    private static boolean containsIgnoreCase(String text, String fragment) {
        return text != null && text.toLowerCase().contains(fragment.toLowerCase());
    }

    private static String sha256(Path file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) >= 0) {
                if (read > 0) digest.update(buffer, 0, read);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    public static class SampleExpectation {
        public String file;
        public String expect;
        public Integer sheets;
        public Integer fields;
        public Integer previewRows;
        public Integer dataRows;
        public String errorContains;
        public Boolean verifyPersisted;
        public String sha256;
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static List<ExcelSheetData> parseExcel(String filename, InputStream inputStream, boolean preview,
                                                   String originFilename) throws Exception {
        Method method = ExcelUtils.class.getDeclaredMethod("parseExcel", String.class, InputStream.class,
                boolean.class, String.class);
        method.setAccessible(true);
        try {
            return (List<ExcelSheetData>) method.invoke(new ExcelUtils(), filename, inputStream, preview, originFilename);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception cause) throw cause;
            throw e;
        }
    }

    private static boolean isEmpty(Path directory) throws IOException {
        try (var files = Files.list(directory)) {
            return files.findAny().isEmpty();
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static class SyntheticRecord {
        @ExcelProperty(index = 0, value = {"i18n_name"})
        private String name;
        @ExcelProperty(index = 1, value = {"i18n_long_number"})
        private long longNumber;
        @ExcelProperty(index = 2, value = {"i18n_decimal"})
        private double decimal;
        @ExcelProperty(index = 3, value = {"i18n_date"})
        private String date;

        public SyntheticRecord() {
        }

        public SyntheticRecord(String name, long longNumber, double decimal, String date) {
            this.name = name;
            this.longNumber = longNumber;
            this.decimal = decimal;
            this.date = date;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public long getLongNumber() { return longNumber; }
        public void setLongNumber(long longNumber) { this.longNumber = longNumber; }
        public double getDecimal() { return decimal; }
        public void setDecimal(double decimal) { this.decimal = decimal; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
    }

    private static class CollectingHandler implements ExcelReadHandler {
        private final Map<Integer, ExcelCell> header = new LinkedHashMap<>();
        private final List<Map<Integer, ExcelCell>> rows = new ArrayList<>();

        @Override
        public void onHeader(Map<Integer, ExcelCell> cells) {
            header.putAll(cells);
        }

        @Override
        public void onRow(int rowIndex, Map<Integer, ExcelCell> cells) {
            rows.add(new LinkedHashMap<>(cells));
        }
    }

    private static class CountingStopHandler implements ExcelReadHandler {
        private int rows;

        @Override
        public void onRow(int rowIndex, Map<Integer, ExcelCell> cells) {
            rows++;
            throw new ExcelReadStopException();
        }
    }

    private static class SyntheticMultipartFile implements MultipartFile {
        private final String filename;
        private final byte[] bytes;

        private SyntheticMultipartFile(String filename, byte[] bytes) {
            this.filename = filename;
            this.bytes = bytes;
        }

        @Override public String getName() { return filename; }
        @Override public String getOriginalFilename() { return filename; }
        @Override public String getContentType() { return "application/vnd.ms-excel"; }
        @Override public boolean isEmpty() { return bytes.length == 0; }
        @Override public long getSize() { return bytes.length; }
        @Override public byte[] getBytes() { return bytes.clone(); }
        @Override public InputStream getInputStream() { return new ByteArrayInputStream(bytes); }
        @Override public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            Files.write(dest.toPath(), bytes);
        }
    }

    private static class FailingOutputStream extends java.io.OutputStream {
        @Override
        public void write(int b) throws IOException {
            throw new IOException("synthetic write failure");
        }
    }

    private static class TrackingInputStream extends InputStream {
        private final InputStream delegate;
        private boolean closed;

        private TrackingInputStream(InputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public int read() throws IOException {
            return delegate.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return delegate.read(b, off, len);
        }

        @Override
        public void close() throws IOException {
            closed = true;
            delegate.close();
        }
    }
}
