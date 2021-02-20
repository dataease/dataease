package io.dataease.excel.domain;

import lombok.Data;

import java.util.List;

@Data
public class ExcelResponse<T> {

    private Boolean success;
    private List<ExcelErrData<T>> errList;

}
