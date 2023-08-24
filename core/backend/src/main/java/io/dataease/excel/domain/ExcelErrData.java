package io.dataease.excel.domain;

import lombok.Data;

@Data
public class ExcelErrData<T> {

    private T t;

    private Integer rowNum;

    private String errMsg;

    public ExcelErrData() {
    }

    public ExcelErrData(T t, Integer rowNum, String errMsg) {
        this.t = t;
        this.rowNum = rowNum;
        this.errMsg = errMsg;
    }
}
