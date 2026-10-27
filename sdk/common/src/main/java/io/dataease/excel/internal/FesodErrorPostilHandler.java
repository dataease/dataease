package io.dataease.excel.internal;

import org.apache.fesod.sheet.write.handler.RowWriteHandler;
import org.apache.fesod.sheet.write.metadata.holder.WriteSheetHolder;
import org.apache.fesod.sheet.write.metadata.holder.WriteTableHolder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import java.util.List;
import java.util.Map;

/**
 * 错误信息批注（红色单元格 + comment），仅在适配层内部使用。
 */
public class FesodErrorPostilHandler implements RowWriteHandler {

    private final List<Map<Integer, String>> errMsgList;

    public FesodErrorPostilHandler(List<Map<Integer, String>> errMsgList) {
        this.errMsgList = errMsgList;
    }

    @Override
    public void afterRowDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row,
                                Integer relativeRowIndex, Boolean isHead) {
        if (isHead != null && isHead) {
            return;
        }
        if (relativeRowIndex == null || relativeRowIndex >= errMsgList.size()) {
            return;
        }
        Sheet sheet = writeSheetHolder.getSheet();
        Map<Integer, String> rowErrMap = errMsgList.get(relativeRowIndex.intValue());
        if (rowErrMap == null) {
            return;
        }
        for (Map.Entry<Integer, String> cellMap : rowErrMap.entrySet()) {
            setPostil(sheet, relativeRowIndex, cellMap.getKey(), cellMap.getValue());
        }
    }

    private void setPostil(Sheet sheet, Integer relativeRowIndex, Integer columnIndex, String msg) {
        Workbook workbook = sheet.getWorkbook();
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        Drawing<?> drawingPatriarch = sheet.createDrawingPatriarch();
        XSSFClientAnchor xssfClientAnchor = new XSSFClientAnchor(0, 0, 0, 0, 0, 0, 2, 2);
        Comment comment = drawingPatriarch.createCellComment(xssfClientAnchor);
        comment.setString(new XSSFRichTextString(msg));
        Row targetRow = sheet.getRow(relativeRowIndex + 1);
        if (targetRow == null) {
            return;
        }
        Cell cell = targetRow.getCell(columnIndex.intValue());
        if (cell == null) {
            return;
        }
        cell.setCellComment(comment);
        cell.setCellStyle(cellStyle);
    }
}
