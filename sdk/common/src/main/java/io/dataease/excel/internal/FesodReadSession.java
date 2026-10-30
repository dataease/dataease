package io.dataease.excel.internal;

import io.dataease.excel.ExcelCell;
import io.dataease.excel.ExcelCellType;
import io.dataease.excel.ExcelIO;
import io.dataease.excel.ExcelReadHandler;
import io.dataease.excel.ExcelReadOptions;
import io.dataease.excel.ExcelReadSession;
import io.dataease.excel.ExcelReadStopException;
import io.dataease.excel.ExcelSheetInfo;
import org.apache.commons.collections4.MapUtils;
import org.apache.fesod.sheet.ExcelReader;
import org.apache.fesod.sheet.context.AnalysisContext;
import org.apache.fesod.sheet.enums.CellDataTypeEnum;
import org.apache.fesod.sheet.event.AnalysisEventListener;
import org.apache.fesod.sheet.exception.ExcelAnalysisStopSheetException;
import org.apache.fesod.sheet.metadata.data.ReadCellData;
import org.apache.fesod.sheet.read.builder.ExcelReaderBuilder;
import org.apache.fesod.sheet.read.metadata.ReadSheet;
import org.apache.fesod.sheet.FesodSheet;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Fesod 读取会话实现，持有 ExcelReader 并复用读取多个 Sheet。
 */
public class FesodReadSession implements ExcelReadSession {

    private final ExcelReader excelReader;
    private final List<ExcelSheetInfo> sheets = new ArrayList<>();
    private final List<ReadSheet> readSheets;
    private final List<Integer> includeColumnIndexes;
    private final InternalListener listener = new InternalListener();
    private ExcelReadHandler currentHandler;

    public FesodReadSession(InputStream inputStream, ExcelReadOptions options) {
        ExcelReaderBuilder builder = FesodSheet.read(inputStream, listener);
        if (options != null) {
            if (options.getHeadRowNumber() != null) {
                builder.headRowNumber(options.getHeadRowNumber());
            }
            if (options.getAutoCloseStream() != null) {
                builder.autoCloseStream(options.getAutoCloseStream());
            }
            if (MapUtils.isNotEmpty(options.getColumnDateFormats())) {
                builder.registerConverter(new FesodColumnDateConverter(options.getColumnDateFormats()));
            }
        }
        this.excelReader = builder.build();
        this.readSheets = excelReader.excelExecutor().sheetList();
        this.includeColumnIndexes = options == null ? null : options.getIncludeColumnIndexes();
        for (ReadSheet readSheet : readSheets) {
            sheets.add(new ExcelSheetInfo(readSheet.getSheetNo(), readSheet.getSheetName()));
        }
    }

    @Override
    public List<ExcelSheetInfo> getSheets() {
        return sheets;
    }

    @Override
    public void read(ExcelSheetInfo sheet, ExcelReadHandler handler) {
        ReadSheet target = readSheets.stream()
                .filter(readSheet -> Objects.equals(readSheet.getSheetNo(), sheet.getSheetNo()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("sheet not found: " + sheet.getSheetName()));
        if (includeColumnIndexes != null && !includeColumnIndexes.isEmpty()) {
            target.setIncludeColumnIndexes(includeColumnIndexes);
        }
        this.currentHandler = handler;
        try {
            excelReader.read(target);
        } finally {
            this.currentHandler = null;
        }
    }

    @Override
    public void close() {
        try {
            excelReader.close();
        } finally {
            ExcelIO.clearThreadLocalCache();
        }
    }

    private class InternalListener extends AnalysisEventListener<Map<Integer, String>> {

        @Override
        public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
            ExcelReadHandler handler = currentHandler;
            if (handler == null) {
                return;
            }
            Map<Integer, ExcelCell> cells = new LinkedHashMap<>();
            for (Map.Entry<Integer, ReadCellData<?>> entry : headMap.entrySet()) {
                ReadCellData<?> cellData = entry.getValue();
                cells.put(entry.getKey(), new ExcelCell(entry.getKey(), mapType(cellData.getType()),
                        cellData.getStringValue(), cellData.getNumberValue(), cellData.getBooleanValue(),
                        cellData.getDataFormatData() == null ? null : cellData.getDataFormatData().getFormat()));
            }
            handler.onHeader(cells);
        }

        @Override
        public void invoke(Map<Integer, String> dataMap, AnalysisContext context) {
            ExcelReadHandler handler = currentHandler;
            if (handler == null) {
                return;
            }
            Map<Integer, ExcelCell> cells = new LinkedHashMap<>();
            for (Map.Entry<Integer, String> entry : dataMap.entrySet()) {
                cells.put(entry.getKey(), new ExcelCell(entry.getKey(), null, entry.getValue(), null, null, null));
            }
            try {
                handler.onRow(context.readRowHolder().getRowIndex(), cells);
            } catch (ExcelReadStopException stop) {
                throw new ExcelAnalysisStopSheetException(stop);
            }
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        }
    }

    private static ExcelCellType mapType(CellDataTypeEnum type) {
        if (type == null) {
            return ExcelCellType.OTHER;
        }
        return switch (type) {
            case STRING, DIRECT_STRING, RICH_TEXT_STRING -> ExcelCellType.STRING;
            case NUMBER -> ExcelCellType.NUMBER;
            case BOOLEAN -> ExcelCellType.BOOLEAN;
            case EMPTY -> ExcelCellType.EMPTY;
            case ERROR -> ExcelCellType.ERROR;
            default -> ExcelCellType.OTHER;
        };
    }
}
