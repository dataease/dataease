package io.dataease.excel.internal;

import io.dataease.excel.ExcelModelHandler;
import org.apache.fesod.sheet.context.AnalysisContext;
import org.apache.fesod.sheet.event.AnalysisEventListener;

/**
 * 模型读取监听器，把 Fesod 回调转换为项目自有 ExcelModelHandler。
 */
public class FesodModelReadListener<T> extends AnalysisEventListener<T> {

    private final ExcelModelHandler<T> handler;

    public FesodModelReadListener(ExcelModelHandler<T> handler) {
        this.handler = handler;
    }

    @Override
    public void invoke(T data, AnalysisContext context) {
        handler.onRow(context.readRowHolder().getRowIndex(), data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        handler.onFinished();
    }
}
