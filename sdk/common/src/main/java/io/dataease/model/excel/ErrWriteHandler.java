package io.dataease.model.excel;

import com.alibaba.excel.write.handler.RowWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import java.util.List;
import java.util.Map;

public class ErrWriteHandler implements RowWriteHandler {

    private List<Map<Integer, String>> errMsgList;

    public ErrWriteHandler(List<Map<Integer, String>> errMsgList) {
        this.errMsgList = errMsgList;
    }

    @Override
    public void afterRowDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row,
                                Integer relativeRowIndex, Boolean isHead) {
        if (!isHead.booleanValue()) {
            Sheet sheet = writeSheetHolder.getSheet();
            Map<Integer, String> rowErrMap = errMsgList.get(relativeRowIndex.intValue());
            for (Map.Entry<Integer, String> cellMap : rowErrMap.entrySet()) {
                setPostil(sheet, relativeRowIndex, cellMap.getKey(), cellMap.getValue());
            }
        }

    }

    private void setPostil(Sheet sheet, Integer relativeRowIndex, Integer i, String msg) {
        Workbook workbook = sheet.getWorkbook();
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        Drawing<?> drawingPatriarch = sheet.createDrawingPatriarch();
        XSSFClientAnchor xssfClientAnchor = new XSSFClientAnchor(0, 0, 0, 0, 0, 0, 2, 2);
        Comment comment = drawingPatriarch.createCellComment(xssfClientAnchor);
        comment.setString(new XSSFRichTextString(msg));
        Cell cell = sheet.getRow(relativeRowIndex + 1).getCell(i.intValue());
        cell.setCellComment(comment);
        cell.setCellStyle(cellStyle);

    }

    @Override
    public void beforeRowCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Integer rowIndex, Integer relativeRowIndex, Boolean isHead) {

    }

    @Override
    public void afterRowCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row, Integer relativeRowIndex, Boolean isHead) {

    }
}
