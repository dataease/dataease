package io.dataease.excel.internal;

import io.dataease.excel.ExcelHeaderComment;
import org.apache.fesod.sheet.write.handler.RowWriteHandler;
import org.apache.fesod.sheet.write.metadata.holder.WriteSheetHolder;
import org.apache.fesod.sheet.write.metadata.holder.WriteTableHolder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import java.util.List;

/**
 * 表头批注绘制，仅在适配层内部使用。
 */
public class FesodHeaderCommentHandler implements RowWriteHandler {

    private final List<ExcelHeaderComment> comments;

    public FesodHeaderCommentHandler(List<ExcelHeaderComment> comments) {
        this.comments = comments;
    }

    @Override
    public void afterRowDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row,
                                Integer relativeRowIndex, Boolean isHead) {
        if (isHead == null || !isHead) {
            return;
        }
        Drawing<?> drawingPatriarch = writeSheetHolder.getSheet().createDrawingPatriarch();
        for (ExcelHeaderComment headerComment : comments) {
            int columnIndex = headerComment.getColumnIndex();
            Comment comment = drawingPatriarch.createCellComment(
                    new XSSFClientAnchor(0, 0, 0, 0, columnIndex + 1, columnIndex, columnIndex + 7, columnIndex + 6));
            comment.setString(new XSSFRichTextString(headerComment.getComment()));
            Cell cell = row.getCell(columnIndex);
            if (cell != null) {
                cell.setCellComment(comment);
            }
        }
    }
}
