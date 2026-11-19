package io.dataease.visualization.manage;

import io.dataease.extensions.view.dto.ChartViewFieldDTO;
import io.dataease.utils.LocalModelUtils;
import io.dataease.visualization.bo.ExcelSheetModel;
import io.dataease.visualization.utils.VisualizationExcelUtils;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Standalone regression: synthetic data only, no Spring context or database. */
public class CoreVisualizationExportManageTest {
    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        Path root = Files.createTempDirectory("report-hidden-columns-");
        System.setProperty("user.home", root.toString());
        Path config = root.resolve("opt/dataease3.0/config/application.yml");
        Files.createDirectories(config.getParent());
        Files.writeString(config, "dataease:\n  path:\n    report: " + root.resolve("reports") + "/\n");
        new LocalModelUtils().setModelValue("desktop");
        Method export = CoreVisualizationExportManage.class.getDeclaredMethod("exportSingleData",
                Map.class, String.class, boolean.class, boolean.class);
        export.setAccessible(true);
        int cases = 0;
        try {
            for (int mask = 0; mask < 8; mask++) {
                for (boolean display : List.of(false, true)) {
                    for (boolean filterHidden : List.of(false, true)) {
                        verify(export, mask, display, filterHidden, false, cases++);
                    }
                }
            }
            verify(export, 2, true, true, true, cases++);
            System.out.println("PASS: " + cases + " actual XLSX exports; headers, values, types and column counts verified");
        } finally {
            try (var paths = Files.walk(root)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(path);
            }
        }
    }

    private static void verify(Method export, int mask, boolean display, boolean filterHidden,
                               boolean sourceData, int caseId) throws Exception {
        List<ChartViewFieldDTO> fields = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        List<String> expectedHeaders = new ArrayList<>();
        List<String> expectedValues = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ChartViewFieldDTO field = new ChartViewFieldDTO();
            field.setId((long) i + 1);
            field.setName("dataset-" + i);
            // Duplicate display names must never merge or shift columns; blank names fall back.
            field.setChartShowName(i == 2 ? " " : "same-display-name");
            field.setDataeaseName("stable-key-" + i);
            field.setDeType(i == 2 ? 3 : 0);
            field.setHide((mask & (1 << i)) != 0);
            fields.add(field);
            String value = i == 2 ? "42.5000" : (i == 1 ? "masked-***" : "synthetic-first");
            row.put(field.getDataeaseName(), value);
            if (!filterHidden || !field.isHide()) {
                expectedHeaders.add(display && i != 2 ? "same-display-name" : field.getName());
                expectedValues.add(i == 2 ? "42.5" : value);
            }
        }
        Map<String, Object> chart = new HashMap<>();
        chart.put("fields", fields);
        chart.put(sourceData ? "sourceData" : "tableRow", List.of(row));
        ExcelSheetModel model = (ExcelSheetModel) export.invoke(new CoreVisualizationExportManage(),
                chart, "synthetic", display, filterHidden);
        if (!model.getHeads().equals(expectedHeaders) || !model.getData().get(0).equals(expectedValues)
                || model.getFiledTypes().size() != expectedHeaders.size()) {
            throw new AssertionError("Column mapping failed: " + caseId);
        }
        var file = VisualizationExcelUtils.exportExcel(List.of(model), "synthetic", "case-" + caseId);
        try (var input = Files.newInputStream(file.toPath()); var workbook = new XSSFWorkbook(input)) {
            var sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            for (int r = 0; r < 2; r++) {
                var actual = sheet.getRow(r);
                List<String> expected = r == 0 ? expectedHeaders : expectedValues;
                if (actual.getPhysicalNumberOfCells() != expected.size()) {
                    throw new AssertionError("Unexpected XLSX column count: " + caseId);
                }
                for (int c = 0; c < expected.size(); c++) {
                    if (!formatter.formatCellValue(actual.getCell(c)).equals(expected.get(c))) {
                        throw new AssertionError("Unexpected XLSX cell: " + caseId + "/" + r + "/" + c);
                    }
                }
            }
        }
    }
}
