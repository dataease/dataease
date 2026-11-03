package io.dataease.visualization.utils;

import io.dataease.constant.DeTypeConstants;
import io.dataease.utils.ConfigUtils;
import io.dataease.utils.FileUtils;
import io.dataease.utils.LogUtil;
import io.dataease.utils.LocalModelUtils;
import io.dataease.visualization.bo.ExcelSheetModel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class VisualizationExcelUtils {

    private static final String suffix = ".xlsx";
    private static final String BASE_ROOT = getBaseRoot();
    // POI 在 createSheet 时会直接抛 IllegalArgumentException：除已有的非法字符外，[ 和 ] 同样不被接受
    private static final Pattern SHEET_NAME_PATTERN = Pattern.compile("[\\s\\\\/:\\*\\?\\\"<>\\|\\[\\]]");
    // POI 与工作表规范的名字长度上限，超出会被静默截断
    private static final int SHEET_NAME_MAX_LENGTH = 31;
    private static final String FALLBACK_SHEET_NAME = "Sheet";
    private static final Map<Workbook, CellStyle> NUMERIC_STYLE_CACHE = new HashMap<>();
    private static final Map<Workbook, CellStyle> INTEGER_STYLE_CACHE = new HashMap<>();

    private static CellStyle getNumericCellStyle(Workbook workbook) {
        return NUMERIC_STYLE_CACHE.computeIfAbsent(workbook, wb -> {
            CellStyle style = wb.createCellStyle();
            style.setDataFormat(wb.createDataFormat().getFormat("#.##########"));
            return style;
        });
    }

    private static CellStyle getIntegerCellStyle(Workbook workbook) {
        return INTEGER_STYLE_CACHE.computeIfAbsent(workbook, wb -> {
            CellStyle style = wb.createCellStyle();
            style.setDataFormat(wb.createDataFormat().getFormat("0"));
            return style;
        });
    }

    public static String getBaseRoot() {
        if (LocalModelUtils.isDesktop()) {
            return ConfigUtils.getConfig("dataease.path.report", "/opt/dataease3.0/data/report/");
        } else {
            return "/opt/dataease3.0/data/report/";
        }
    }

    /**
     * 图表标题会直接作为工作表名，POI 校验不通过时整份导出都会失败。
     * 除替换非法字符外，首尾的单引号也必须去掉，清洗后为空或重名时还要兜底。
     */
    private static String toSheetName(String rawName, Set<String> usedNames) {
        String sheetName = StringUtils.isBlank(rawName) ? null : SHEET_NAME_PATTERN.matcher(rawName).replaceAll("-");
        sheetName = stripOuterApostrophes(sheetName);
        if (StringUtils.isBlank(sheetName)) {
            sheetName = FALLBACK_SHEET_NAME;
        } else if (sheetName.length() > SHEET_NAME_MAX_LENGTH) {
            // 自己按上限截断，后面的重名判断才和 POI 实际写入的名字一致
            sheetName = stripOuterApostrophes(sheetName.substring(0, SHEET_NAME_MAX_LENGTH));
        }
        return nextUniqueSheetName(sheetName, usedNames);
    }

    private static String stripOuterApostrophes(String source) {
        if (source == null) {
            return null;
        }
        int start = 0;
        int end = source.length();
        while (start < end && source.charAt(start) == '\'') {
            start++;
        }
        while (end > start && source.charAt(end - 1) == '\'') {
            end--;
        }
        return source.substring(start, end);
    }

    /**
     * POI 按大小写不敏感校验工作表名唯一，不同标题清洗成同一个名字后会抛出
     * "The workbook already contains a sheet named ..."，所以追加序号，并让序号落在长度上限以内。
     */
    private static String nextUniqueSheetName(String sheetName, Set<String> usedNames) {
        String candidate = sheetName;
        for (int seq = 1; !usedNames.add(candidate.toLowerCase(Locale.ROOT)); seq++) {
            String marker = "-" + seq;
            int keep = Math.max(SHEET_NAME_MAX_LENGTH - marker.length(), 0);
            candidate = sheetName.substring(0, Math.min(sheetName.length(), keep)) + marker;
        }
        return candidate;
    }

    public static File exportExcel(List<ExcelSheetModel> sheets, String fileName, String folderId) throws Exception {
        Pattern pattern = Pattern.compile("[\\s\\\\/:\\*\\?\\\"<>.\\|]");
        AtomicReference<String> realFileName = new AtomicReference<>(fileName);
        if (StringUtils.isNotBlank(fileName)) {
            realFileName.set(pattern.matcher(StringUtils.removeEnd(fileName, suffix)).replaceAll("-"));
        }
        SXSSFWorkbook wb = new SXSSFWorkbook();
        Set<String> usedSheetNames = new HashSet<>();

        sheets.forEach(sheet -> {

            List<List<String>> details = sheet.getData();
            List<Integer> fieldTypes = sheet.getFiledTypes();
            details.add(0, sheet.getHeads());
            String sheetName = toSheetName(sheet.getSheetName(), usedSheetNames);
            Sheet curSheet = wb.createSheet(sheetName);
            if (StringUtils.isBlank(fileName)) {
                String cName = pattern.matcher(sheetName).replaceAll("-");
                realFileName.set(cName);
            }

            CellStyle cellStyle = wb.createCellStyle();
            Font font = wb.createFont();
            font.setFontHeightInPoints((short) 12);
            font.setBold(true);
            cellStyle.setFont(font);
            cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            if (CollectionUtils.isNotEmpty(details)) {
                for (int i = 0; i < details.size(); i++) {
                    Row row = curSheet.createRow(i);
                    List<String> rowData = details.get(i);
                    if (rowData != null) {
                        for (int j = 0; j < rowData.size(); j++) {
                            Cell cell = row.createCell(j);
                            // with DataType
                            if (i > 0 && (fieldTypes.get(j).equals(DeTypeConstants.DE_INT) || fieldTypes.get(j).equals(DeTypeConstants.DE_FLOAT)) && StringUtils.isNotEmpty(rowData.get(j))) {
                                BigDecimal decimal = new BigDecimal(rowData.get(j)).stripTrailingZeros();
                                if (decimal.scale() <= 0) {
                                    cell.setCellStyle(getIntegerCellStyle(wb));
                                } else {
                                    cell.setCellStyle(getNumericCellStyle(wb));
                                }
                                cell.setCellValue(decimal.doubleValue());
                            } else {
                                cell.setCellValue(rowData.get(j));
                            }
                            if (i == 0) {// 头部
                                cell.setCellStyle(cellStyle);
                                // 设置列的宽度
                                curSheet.setColumnWidth(j, 255 * 20);
                            }
                        }
                    }
                }
            }
        });
        realFileName.set(realFileName.get() + suffix);
        String folderPath = BASE_ROOT;
        if (StringUtils.isNotBlank(folderId)) {
            folderPath = BASE_ROOT + folderId + "/";
        }

        folderPath += Thread.currentThread().getId() + "/";
        FileUtils.validateExist(folderPath);
        File result = new File(folderPath + realFileName.get()).getCanonicalFile();
        FileOutputStream fos = new FileOutputStream(result);
        BufferedOutputStream outputStream = new BufferedOutputStream(fos);
        try {
            wb.write(outputStream);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), new Throwable(e));
            throw e;
        } finally {
            wb.dispose();
            wb.close();
            outputStream.flush();
            outputStream.close();
        }
        return result;
    }
}
