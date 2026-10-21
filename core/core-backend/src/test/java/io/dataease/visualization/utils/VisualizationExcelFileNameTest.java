package io.dataease.visualization.utils;

import io.dataease.utils.LocalModelUtils;
import io.dataease.visualization.bo.ExcelSheetModel;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Standalone regression entry point. Run in a separate JVM with a temporary user.home. */
public class VisualizationExcelFileNameTest {
    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        Path root = Files.createTempDirectory("report-filename-");
        System.setProperty("user.home", root.toString());
        Path config = root.resolve("opt/dataease3.0/config/application.yml");
        Files.createDirectories(config.getParent());
        Files.writeString(config, "dataease:\n  path:\n    report: " + root.resolve("reports") + "/\n");
        new LocalModelUtils().setModelValue("desktop");
        String[][] cases = {
                {"report", "report.xlsx"}, {"report.xlsx", "report.xlsx"},
                {"业务.大盘.v1", "业务-大盘-v1.xlsx"}, {"业务.大盘.v1.xlsx", "业务-大盘-v1.xlsx"},
                {"report.", "report-.xlsx"}, {"report..v1", "report--v1.xlsx"},
                {"a/b\\c:d*e?f\"g<h>i|j k", "a-b-c-d-e-f-g-h-i-j-k.xlsx"},
                {null, "工作表-v1.xlsx"}, {"", "工作表-v1.xlsx"}, {"  ", "工作表-v1.xlsx"}
        };
        try {
            for (int i = 0; i < cases.length; i++) {
                ExcelSheetModel sheet = new ExcelSheetModel();
                sheet.setSheetName("工作表.v1");
                sheet.setHeads(List.of("test"));
                sheet.setData(new ArrayList<>());
                sheet.setFiledTypes(List.of());
                var file = VisualizationExcelUtils.exportExcel(List.of(sheet), cases[i][0], "case-" + i);
                if (!file.getName().equals(cases[i][1])) throw new AssertionError(file.getName());
                try (var input = Files.newInputStream(file.toPath()); var workbook = new XSSFWorkbook(input)) {
                    if (!workbook.getSheetName(0).equals("工作表.v1")) throw new AssertionError("sheet renamed");
                    if (!workbook.getSheetAt(0).getRow(0).getCell(0).getStringCellValue().equals("test")) {
                        throw new AssertionError("content changed");
                    }
                }
            }
            System.out.println("PASS: 10 actual Excel exports; extensions, sheet names and contents verified");
        } finally {
            try (var paths = Files.walk(root)) {
                for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) Files.delete(path);
            }
        }
    }
}
