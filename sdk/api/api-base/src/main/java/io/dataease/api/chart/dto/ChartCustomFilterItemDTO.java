package io.dataease.api.chart.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author Junjun
 */
@Data
public class ChartCustomFilterItemDTO implements Serializable {
    private Long fieldId;
    private String term;
    private String value;
}
