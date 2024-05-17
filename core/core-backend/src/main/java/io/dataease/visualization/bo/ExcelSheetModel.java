package io.dataease.visualization.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class ExcelSheetModel implements Serializable {
    @Serial
    private static final long serialVersionUID = 1122095875367371623L;

    private String sheetName;

    private List<String> heads;

    private List<List<String>> data;

    private List<Integer> filedTypes;
}
