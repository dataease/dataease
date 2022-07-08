package io.dataease.commons.utils;

import java.io.File;
import io.dataease.commons.model.excel.ExcelSheetModel;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;

import cn.hutool.core.util.IdUtil;

public class ExcelUtils {

    public static File exportExcel(List<ExcelSheetModel> sheets) throws Exception {
        String fastUUID = IdUtil.fastUUID();
        File result = new File("/opt/dataease/data/" + fastUUID + ".xls");
        HSSFWorkbook wb = new HSSFWorkbook();
        sheets.forEach(sheet -> {
            List<List<String>> details = sheet.getDatas();
            details.add(0, sheet.getHeads());
            HSSFSheet curSheet = wb.createSheet(sheet.getSheetName());
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
        wb.write(result);
        return result;
    }

}
