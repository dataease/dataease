package io.dataease.excel.internal;

import io.dataease.i18n.Translator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.fesod.sheet.constant.OrderConstant;
import org.apache.fesod.sheet.enums.CellDataTypeEnum;
import org.apache.fesod.sheet.metadata.Head;
import org.apache.fesod.sheet.metadata.data.WriteCellData;
import org.apache.fesod.sheet.write.handler.context.CellWriteHandlerContext;
import org.apache.fesod.sheet.write.metadata.holder.WriteSheetHolder;
import org.apache.fesod.sheet.write.style.column.AbstractColumnWidthStyleStrategy;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 列宽自适应 + 表头 i18n 转换，仅在适配层内部使用。
 */
public class FesodAutoWidthStyleStrategy extends AbstractColumnWidthStyleStrategy {

    private static final int MIN_COLUMN_WIDTH = 8;

    private static final int MAX_COLUMN_WIDTH = 50;

    private static final int PADDING_WIDTH = 2;

    private static final int MAX_COLUMN_WIDTH_UNITS = 255 * 256;

    private final Map<Integer, Map<Integer, Integer>> maxColumnWidthMap = new HashMap<>(8);

    private final Map<Integer, CellStyle> wrapCellStyleCache = new HashMap<>(8);

    private final DataFormatter dataFormatter = new DataFormatter();

    public FesodAutoWidthStyleStrategy() {
    }

    @Override
    public int order() {
        // 在 FillStyleCellWriteHandler 之后执行，避免最终填充样式覆盖自动换行。
        return OrderConstant.FILL_STYLE + 1;
    }

    @Override
    protected void setColumnWidth(WriteSheetHolder writeSheetHolder, List<WriteCellData<?>> cellDataList, Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
        boolean headCell = Boolean.TRUE.equals(isHead);
        boolean needSetWidth = headCell || !CollectionUtils.isEmpty(cellDataList);
        if (!needSetWidth || cell == null) {
            return;
        }
        String text = resolveText(cellDataList, cell, headCell);
        if (text == null) {
            return;
        }
        int columnWidth = displayWidth(text);
        int columnIndex = cell.getColumnIndex();
        Map<Integer, Integer> sheetWidthMap = maxColumnWidthMap.computeIfAbsent(writeSheetHolder.getSheetNo(), key -> new HashMap<>(16));
        Integer maxColumnWidth = sheetWidthMap.get(columnIndex);
        if (maxColumnWidth == null || columnWidth > maxColumnWidth) {
            sheetWidthMap.put(columnIndex, columnWidth);
            writeSheetHolder.getSheet().setColumnWidth(columnIndex, toColumnWidthUnits(columnWidth));
        }
        applyWrapTextIfNeeded(cell, text, columnWidth);
    }

    private String resolveText(List<WriteCellData<?>> cellDataList, Cell cell, boolean isHead) {
        if (isHead) {
            return cell.getStringCellValue();
        }
        if (CollectionUtils.isEmpty(cellDataList)) {
            return null;
        }
        WriteCellData<?> cellData = cellDataList.get(0);
        CellDataTypeEnum type = cellData.getType();
        if (type == null) {
            return null;
        }
        switch (type) {
            case STRING:
            case DIRECT_STRING:
                return cellData.getStringValue();
            case RICH_TEXT_STRING:
                return resolveRichText(cellData);
            case BOOLEAN:
                Boolean booleanValue = cellData.getBooleanValue();
                return booleanValue == null ? null : booleanValue.toString();
            case NUMBER:
            case DATE:
                return dataFormatter.formatCellValue(cell);
            default:
                return null;
        }
    }

    private String resolveRichText(WriteCellData<?> cellData) {
        String text = cellData.getStringValue();
        if (text != null) {
            return text;
        }
        if (cellData.getRichTextStringDataValue() != null) {
            return cellData.getRichTextStringDataValue().getTextString();
        }
        return null;
    }

    private int displayWidth(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        int maxLineWidth = 0;
        int lineWidth = 0;
        for (int offset = 0; offset < text.length(); ) {
            int codePoint = text.codePointAt(offset);
            if (codePoint == '\n' || codePoint == '\r') {
                if (codePoint == '\r' && offset + 1 < text.length() && text.charAt(offset + 1) == '\n') {
                    offset++;
                }
                maxLineWidth = Math.max(maxLineWidth, lineWidth);
                lineWidth = 0;
            } else {
                lineWidth += isFullWidth(codePoint) ? 2 : 1;
            }
            offset += Character.charCount(codePoint);
        }
        return Math.max(maxLineWidth, lineWidth);
    }

    private boolean isFullWidth(int codePoint) {
        // CJK 及全角字符按 2 个标准字符宽度计算，其余按 1 个计算，避免依赖系统默认编码的字节数。
        return (codePoint >= 0x1100 && codePoint <= 0x115F)
                || (codePoint >= 0x2E80 && codePoint <= 0x303E)
                || (codePoint >= 0x3041 && codePoint <= 0x33FF)
                || (codePoint >= 0x3400 && codePoint <= 0x4DBF)
                || (codePoint >= 0x4E00 && codePoint <= 0x9FFF)
                || (codePoint >= 0xA000 && codePoint <= 0xA4CF)
                || (codePoint >= 0xAC00 && codePoint <= 0xD7AF)
                || (codePoint >= 0xF900 && codePoint <= 0xFAFF)
                || (codePoint >= 0xFE30 && codePoint <= 0xFE4F)
                || (codePoint >= 0xFF00 && codePoint <= 0xFF60)
                || (codePoint >= 0xFFE0 && codePoint <= 0xFFE6)
                || (codePoint >= 0x20000 && codePoint <= 0x2FA1F);
    }

    private int toColumnWidthUnits(int columnWidth) {
        int targetWidth = Math.min(MAX_COLUMN_WIDTH, Math.max(MIN_COLUMN_WIDTH, columnWidth)) + PADDING_WIDTH;
        return Math.min(MAX_COLUMN_WIDTH_UNITS, targetWidth * 256);
    }

    private void applyWrapTextIfNeeded(Cell cell, String text, int columnWidth) {
        boolean wrapNeeded = columnWidth > MAX_COLUMN_WIDTH || text.indexOf('\n') >= 0 || text.indexOf('\r') >= 0;
        if (!wrapNeeded) {
            return;
        }
        CellStyle originalStyle = cell.getCellStyle();
        if (originalStyle != null && originalStyle.getWrapText()) {
            return;
        }
        Integer styleKey = originalStyle == null ? -1 : Integer.valueOf(originalStyle.getIndex());
        CellStyle wrapStyle = wrapCellStyleCache.get(styleKey);
        if (wrapStyle == null) {
            Workbook workbook = cell.getSheet().getWorkbook();
            wrapStyle = workbook.createCellStyle();
            if (originalStyle != null) {
                wrapStyle.cloneStyleFrom(originalStyle);
            }
            wrapStyle.setWrapText(true);
            wrapCellStyleCache.put(styleKey, wrapStyle);
        }
        cell.setCellStyle(wrapStyle);
    }

    @Override
    public void beforeCellCreate(CellWriteHandlerContext context) {
        if (!context.getHead()) return;
        Head headData = context.getHeadData();
        List<String> headNameList = null;
        if (ObjectUtils.isNotEmpty(headData) && CollectionUtils.isNotEmpty(headNameList = headData.getHeadNameList())) {
            List<String> i18nNameList = headNameList.stream().map(Translator::get).collect(Collectors.toList());
            context.getHeadData().setHeadNameList(i18nNameList);
        }
    }
}
