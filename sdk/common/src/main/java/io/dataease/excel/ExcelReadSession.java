package io.dataease.excel;

import java.util.List;

/**
 * 多 Sheet 读取会话：同一读取器可复用于多个 Sheet，close 负责释放读取器与线程本地缓存。
 */
public interface ExcelReadSession extends AutoCloseable {

    List<ExcelSheetInfo> getSheets();

    void read(ExcelSheetInfo sheet, ExcelReadHandler handler);

    @Override
    void close();
}
