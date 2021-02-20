package io.dataease.excel.domain;

import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

public class TestCaseExcelDataFactory implements ExcelDataFactory {
    @Override
    public Class getExcelDataByLocal() {
        Locale locale = LocaleContextHolder.getLocale();
        if (Locale.US.toString().equalsIgnoreCase(locale.toString())) {
            return TestCaseExcelDataUs.class;
        } else if (Locale.TRADITIONAL_CHINESE.toString().equalsIgnoreCase(locale.toString())) {
            return TestCaseExcelDataTw.class;
        }
        return TestCaseExcelDataCn.class;
    }
}
