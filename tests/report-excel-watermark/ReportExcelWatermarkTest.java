import io.dataease.commons.utils.ExcelWatermarkUtils;
import io.dataease.api.permissions.user.vo.UserFormVO;
import io.dataease.utils.LocalModelUtils;
import io.dataease.visualization.bo.ExcelSheetModel;
import io.dataease.visualization.dto.WatermarkContentDTO;
import io.dataease.visualization.utils.VisualizationExcelUtils;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import java.nio.file.*;
import java.util.*;

public class ReportExcelWatermarkTest {
    private static void verifyPolicy() throws Exception {
        var manager = new io.dataease.exportCenter.manage.ExportCenterDownLoadManage();
        var config = new io.dataease.visualization.dao.auto.entity.VisualizationWatermark();
        var repositoryType = io.dataease.visualization.dao.auto.mapper.VisualizationWatermarkRepository.class;
        Object repository = java.lang.reflect.Proxy.newProxyInstance(repositoryType.getClassLoader(),
                new Class<?>[]{repositoryType}, (proxy, method, args) -> Optional.of(config));
        var repositoryField = manager.getClass().getDeclaredField("watermarkRepository");
        repositoryField.setAccessible(true);
        repositoryField.set(manager, repository);
        var mapperField = manager.getClass().getDeclaredField("objectMapper");
        mapperField.setAccessible(true);
        mapperField.set(manager, new com.fasterxml.jackson.databind.ObjectMapper());
        var context = new org.springframework.context.support.StaticApplicationContext();
        var userType = io.dataease.api.permissions.user.api.UserApi.class;
        Object api = java.lang.reflect.Proxy.newProxyInstance(userType.getClassLoader(), new Class<?>[]{userType},
                (proxy, method, args) -> {
                    UserFormVO user = new UserFormVO();
                    user.setName("Synthetic");
                    user.setAccount("synthetic-user");
                    return user;
                });
        context.getBeanFactory().registerSingleton("syntheticUserApi", api);
        new io.dataease.utils.CommonBeanFactory().setApplicationContext(context);
        try {
            for (String json : List.of("null", "{}", "{\"enable\":false,\"excelEnable\":true}",
                    "{\"enable\":true,\"excelEnable\":false}",
                    "{\"enable\":true,\"excelEnable\":true,\"type\":\"custom\",\"content\":\"SYNTHETIC\",\"watermark_fontsize\":16}")) {
                config.setSettingContent(json);
                try (var wb = new org.apache.poi.xssf.streaming.SXSSFWorkbook()) {
                    var sheet = wb.createSheet("Policy");
                    sheet.createRow(0).createCell(0).setCellValue("synthetic");
                    manager.addWatermarkTools(wb, Map.of(sheet, new int[]{0, 1}));
                    boolean expected = json.contains("\"enable\":true,\"excelEnable\":true");
                    if (wb.getAllPictures().size() != (expected ? 1 : 0)) throw new AssertionError("policy: " + json);
                    wb.dispose();
                }
            }
        } finally {
            new io.dataease.utils.CommonBeanFactory().setApplicationContext(null);
            context.close();
        }
    }

    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("report-watermark-");
        System.setProperty("user.home", root.toString());
        Path config = root.resolve("opt/dataease3.0/config/application.yml");
        Files.createDirectories(config.getParent());
        Files.writeString(config, "dataease:\n  path:\n    report: " + root.resolve("reports") + "/\n");
        new LocalModelUtils().setModelValue("desktop");
        try {
            verifyPolicy();
            for (boolean enabled : List.of(false, true, true)) {
                List<ExcelSheetModel> sheets = new ArrayList<>();
                for (int n = 0; n < 2; n++) {
                    ExcelSheetModel model = new ExcelSheetModel();
                    model.setSheetName("Synthetic " + n);
                    model.setHeads(Collections.nCopies(24, "header"));
                    model.setFiledTypes(Collections.nCopies(24, 0));
                    List<List<String>> rows = new ArrayList<>();
                    // Wide first row has been flushed by SXSSF before the short tail is written.
                    rows.add(Collections.nCopies(24, "synthetic"));
                    for (int row = 1; row < 5000; row++) rows.add(List.of("value"));
                    model.setData(rows);
                    sheets.add(model);
                }
                var file = VisualizationExcelUtils.exportExcel(sheets, "synthetic", "test",
                        enabled ? (wb, bounds) -> {
                            WatermarkContentDTO setting = new WatermarkContentDTO();
                            setting.setType("custom");
                            setting.setContent("SYNTHETIC ${username}");
                            setting.setWatermark_fontsize(16);
                            setting.setWatermark_color("rgb(100, 100, 100)");
                            UserFormVO user = new UserFormVO();
                            user.setName("Synthetic");
                            user.setAccount("synthetic-user");
                            int picture = ExcelWatermarkUtils.addWatermarkImage(wb, setting, user);
                            bounds.forEach((sheet, size) -> {
                                if (size[0] != 5000 || size[1] != 24) throw new AssertionError("lost flushed bounds");
                                ExcelWatermarkUtils.addWatermarkToSheet(sheet, picture, size[0], size[1]);
                            });
                        } : null);
                try (XSSFWorkbook wb = (XSSFWorkbook) WorkbookFactory.create(file)) {
                    if (wb.getAllPictures().size() != (enabled ? 1 : 0)) throw new AssertionError("image dedup/off");
                    for (var sheet : wb) {
                        if (sheet.getLastRowNum() != 5000) throw new AssertionError("data truncated");
                        XSSFDrawing drawing = (XSSFDrawing) sheet.getDrawingPatriarch();
                        if (!enabled) {
                            if (drawing != null) throw new AssertionError("unexpected watermark");
                            continue;
                        }
                        int count = 0;
                        boolean bottom = false, right = false;
                        for (var shape : drawing.getShapes()) {
                            XSSFPicture picture = (XSSFPicture) shape;
                            bottom |= picture.getClientAnchor().getRow1() >= 4995;
                            right |= picture.getClientAnchor().getCol1() >= 24;
                            count++;
                        }
                        if (!bottom || !right || count != 1336) throw new AssertionError("coverage/duplicate: " + count);
                    }
                }
                String previewPath = System.getenv("REPORT_WATERMARK_PREVIEW");
                if (enabled && previewPath != null) {
                    Files.copy(file.toPath(), Path.of(previewPath), StandardCopyOption.REPLACE_EXISTING);
                }
                Files.delete(file.toPath());
            }
            System.out.println("PASS: enabled/disabled/repeated, two sheets, 5000 rows, flushed wide row, shared image, full coverage");
        } finally {
            try (var paths = Files.walk(root)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
            }
        }
    }
}
