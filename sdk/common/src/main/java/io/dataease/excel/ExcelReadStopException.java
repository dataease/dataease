package io.dataease.excel;

/**
 * 业务方在读取回调中抛出以提前停止当前 Sheet，由适配层转换为 Fesod 的停止异常。
 */
public class ExcelReadStopException extends RuntimeException {

    public ExcelReadStopException() {
    }
}
