package io.dataease.commons.utils;

import java.io.File;

import io.dataease.commons.model.excel.ExcelSheetModel;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;


public class ExcelUtils {
    private static final String suffix = ".xls";

    public static File exportExcel(List<ExcelSheetModel> sheets, String fileName) throws Exception {
        AtomicReference<String> realFileName = new AtomicReference<>(fileName);
        HSSFWorkbook wb = new HSSFWorkbook();

        sheets.forEach(sheet -> {

            List<List<String>> details = sheet.getDatas();
            details.add(0, sheet.getHeads());
            String sheetName = sheet.getSheetName();
            HSSFSheet curSheet = wb.createSheet(sheetName);
            if (StringUtils.isBlank(fileName)) {
                String cName = sheetName + suffix;
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
                    HSSFRow row = curSheet.createRow(i);
                    List<String> rowData = details.get(i);
                    if (rowData != null) {
                        for (int j = 0; j < rowData.size(); j++) {
                            HSSFCell cell = row.createCell(j);
                            cell.setCellValue(rowData.get(j));
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
        if (!StringUtils.endsWith(fileName, suffix)) {
            realFileName.set(realFileName.get() + suffix);
        }
        File result = new File("/opt/dataease/data/" + realFileName.get());
        wb.write(result);
        return result;
    }

}
