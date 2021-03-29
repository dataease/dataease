package io.dataease.dto.chart;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Author gin
 * @Date 2021/3/25 10:31 上午
 */
@Getter
@Setter
public class ChartViewFieldFilterDTO implements Serializable {
    private String term;
    private String value;
}
