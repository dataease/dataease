package io.dataease.api.chart.request;

import io.dataease.api.chart.dto.ViewDetailField;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;


@Data
public class ChartExcelRequestInner implements Serializable {
    @Serial
    private static final long serialVersionUID = -8655439241248268940L;

    private String[] header;

    private Integer[] excelTypes;

    private List<Object[]> details;

    private ViewDetailField[] detailFields;

    private List<String> excelHeaderKeys;

}
