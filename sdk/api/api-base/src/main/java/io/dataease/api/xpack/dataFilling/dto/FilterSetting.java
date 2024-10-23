package io.dataease.api.xpack.dataFilling.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class FilterSetting implements Serializable {
    @Serial
    private static final long serialVersionUID = -3292560414617242094L;

    private String column;
    private Object value;
    private List<String> valueList;
    private BigDecimal min;
    private BigDecimal max;
    private boolean number;
    private boolean date;
    private String dateType;
    private String filterType;
    private String term;
    private boolean multiple;
    private String type;
}
