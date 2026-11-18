package io.dataease.excel;

import io.dataease.excel.internal.FesodAutoWidthStyleStrategy;
import io.dataease.excel.internal.FesodColumnDateConverter;
import io.dataease.excel.internal.FesodErrorPostilHandler;
import io.dataease.excel.internal.FesodHeaderCommentHandler;
import io.dataease.excel.internal.FesodModelReadListener;
import io.dataease.excel.internal.FesodReadSession;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.fesod.sheet.ExcelReader;
import org.apache.fesod.sheet.FesodSheet;
import org.apache.fesod.sheet.read.builder.ExcelReaderBuilder;
import org.apache.fesod.sheet.read.builder.ExcelReaderSheetBuilder;
import org.apache.fesod.sheet.read.metadata.ReadSheet;
import org.apache.fesod.sheet.util.DateUtils;
import org.apache.fesod.sheet.util.NumberDataFormatterUtils;
import org.apache.fesod.sheet.write.builder.ExcelWriterBuilder;
import org.apache.fesod.sheet.write.builder.ExcelWriterSheetBuilder;
import org.apache.fesod.sheet.write.metadata.style.WriteCellStyle;
import org.apache.fesod.sheet.write.metadata.style.WriteFont;
import org.apache.fesod.sheet.write.style.HorizontalCellStyleStrategy;
import org.apache.poi.ss.usermodel.IndexedColors;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Excel 适配层唯一入口，集中封装 Fesod 读写与流生命周期，业务模块不得直接依赖 Fesod 类型。
 */
public final class ExcelIO {

    private ExcelIO() {
    }

    public static ExcelReadSession openRead(InputStream inputStream, ExcelReadOptions options) {
        return new FesodReadSession(inputStream, options == null ? new ExcelReadOptions() : options);
    }

    /**
     * 读取单个 Sheet（默认第一个），结束后释放读取器并清理线程本地缓存。
     */
    public static void read(InputStream inputStream, ExcelReadOptions options, ExcelReadHandler handler) {
        ExcelReadOptions readOptions = options == null ? new ExcelReadOptions() : options;
        try (ExcelReadSession session = openRead(inputStream, readOptions)) {
            ExcelSheetInfo target = resolveTarget(session.getSheets(), readOptions);
            session.read(target, handler);
        }
    }

    public static <T> void readModel(InputStream inputStream, Class<T> modelClass, ExcelReadOptions options, ExcelModelHandler<T> handler) {
        ExcelReadOptions readOptions = options == null ? new ExcelReadOptions() : options;
        if (readOptions.getIncludeColumnIndexes() != null && !readOptions.getIncludeColumnIndexes().isEmpty()) {
            throw new IllegalArgumentException("includeColumnIndexes is not supported by readModel");
        }
        try {
            ExcelReaderBuilder readerBuilder = FesodSheet.read(inputStream, modelClass, new FesodModelReadListener<>(handler));
            if (readOptions.getAutoCloseStream() != null) {
                readerBuilder.autoCloseStream(readOptions.getAutoCloseStream());
            }
            if (readOptions.getColumnDateFormats() != null && !readOptions.getColumnDateFormats().isEmpty()) {
                readerBuilder.registerConverter(new FesodColumnDateConverter(readOptions.getColumnDateFormats(), null));
            }
            ExcelReader reader = readerBuilder.build();
            List<ReadSheet> readSheets = reader.excelExecutor().sheetList();
            List<ExcelSheetInfo> sheetInfos = readSheets.stream()
                    .map(readSheet -> new ExcelSheetInfo(readSheet.getSheetNo(), readSheet.getSheetName()))
                    .collect(Collectors.toList());
            ExcelSheetInfo target = resolveTarget(sheetInfos, readOptions);
            ReadSheet targetSheet = readSheets.stream()
                    .filter(readSheet -> Objects.equals(readSheet.getSheetNo(), target.getSheetNo()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("sheet not found: " + target.getSheetName()));
            ExcelReaderSheetBuilder sheetBuilder = new ExcelReaderSheetBuilder(reader);
            sheetBuilder.sheetNo(targetSheet.getSheetNo());
            if (readOptions.getHeadRowNumber() != null) {
                sheetBuilder.headRowNumber(readOptions.getHeadRowNumber());
            }
            sheetBuilder.doRead();
        } finally {
            clearThreadLocalCache();
        }
    }

    public static void writeModel(OutputStream outputStream, Class<?> modelClass, List<?> data, ExcelWriteOptions options) {
        ExcelWriteOptions writeOptions = options == null ? new ExcelWriteOptions() : options;
        if (CollectionUtils.isNotEmpty(writeOptions.getHead()) || writeOptions.getAutomaticMergeHead() != null) {
            throw new IllegalArgumentException("head/automaticMergeHead are not supported by writeModel");
        }
        try {
            ExcelWriterBuilder writerBuilder = FesodSheet.write(outputStream, modelClass);
            if (writeOptions.getInMemory() != null) {
                writerBuilder.inMemory(writeOptions.getInMemory());
            }
            if (writeOptions.getAutoCloseStream() != null) {
                writerBuilder.autoCloseStream(writeOptions.getAutoCloseStream());
            }
            if (CollectionUtils.isNotEmpty(writeOptions.getErrMsgList())) {
                writerBuilder.inMemory(Boolean.TRUE).registerWriteHandler(new FesodErrorPostilHandler(writeOptions.getErrMsgList()));
            }
            if (CollectionUtils.isNotEmpty(writeOptions.getHeaderComments())) {
                writerBuilder.registerWriteHandler(new FesodHeaderCommentHandler(writeOptions.getHeaderComments()));
            }
            writerBuilder.registerWriteHandler(new FesodAutoWidthStyleStrategy())
                    .registerWriteHandler(defaultStyleStrategy());
            ExcelWriterSheetBuilder sheetBuilder = writerBuilder.sheet(writeOptions.getSheetName());
            sheetBuilder.doWrite(data == null ? Collections.emptyList() : data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeTemplate(OutputStream outputStream, ExcelWriteOptions options) {
        ExcelWriteOptions writeOptions = options == null ? new ExcelWriteOptions() : options;
        if (CollectionUtils.isNotEmpty(writeOptions.getErrMsgList())) {
            throw new IllegalArgumentException("errMsgList is not supported by writeTemplate");
        }
        try {
            ExcelWriterBuilder writerBuilder = FesodSheet.write(outputStream);
            if (CollectionUtils.isNotEmpty(writeOptions.getHead())) {
                writerBuilder.head(writeOptions.getHead());
            }
            if (writeOptions.getAutomaticMergeHead() != null) {
                writerBuilder.automaticMergeHead(writeOptions.getAutomaticMergeHead());
            }
            if (writeOptions.getInMemory() != null) {
                writerBuilder.inMemory(writeOptions.getInMemory());
            }
            if (writeOptions.getAutoCloseStream() != null) {
                writerBuilder.autoCloseStream(writeOptions.getAutoCloseStream());
            }
            if (CollectionUtils.isNotEmpty(writeOptions.getHeaderComments())) {
                writerBuilder.registerWriteHandler(new FesodHeaderCommentHandler(writeOptions.getHeaderComments()));
            }
            writerBuilder.registerWriteHandler(new FesodAutoWidthStyleStrategy());
            ExcelWriterSheetBuilder sheetBuilder = writerBuilder.sheet(writeOptions.getSheetName());
            sheetBuilder.doWrite(Collections.emptyList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 清理 Fesod 缓存在当前线程内的日期/数字格式判断结果。
     */
    public static void clearThreadLocalCache() {
        DateUtils.removeThreadLocalCache();
        NumberDataFormatterUtils.removeThreadLocalCache();
    }

    private static HorizontalCellStyleStrategy defaultStyleStrategy() {
        WriteCellStyle headCellStyle = new WriteCellStyle();
        headCellStyle.setFillForegroundColor(Short.valueOf(IndexedColors.WHITE.getIndex()));
        WriteFont headFont = new WriteFont();
        headFont.setFontHeightInPoints((short) 12);
        headCellStyle.setWriteFont(headFont);
        WriteCellStyle contentCellStyle = new WriteCellStyle();
        WriteFont contentFont = new WriteFont();
        contentFont.setFontHeightInPoints((short) 11);
        contentCellStyle.setWriteFont(contentFont);
        return new HorizontalCellStyleStrategy(headCellStyle, contentCellStyle);
    }

    private static ExcelSheetInfo resolveTarget(List<ExcelSheetInfo> sheets, ExcelReadOptions options) {
        if (sheets == null || sheets.isEmpty()) {
            throw new IllegalStateException("no sheet found");
        }
        if (options.getSheetName() != null) {
            return sheets.stream()
                    .filter(sheet -> options.getSheetName().equals(sheet.getSheetName()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("sheet not found: " + options.getSheetName()));
        }
        if (options.getSheetNo() != null) {
            return sheets.stream()
                    .filter(sheet -> options.getSheetNo().equals(sheet.getSheetNo()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("sheet not found: " + options.getSheetNo()));
        }
        return sheets.get(0);
    }
}
