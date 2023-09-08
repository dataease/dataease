package io.dataease.dto.chart;

import io.dataease.plugins.common.base.domain.DatasetTableField;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Author gin
 * @Date 2021/5/21 4:24 下午
 */
@Getter
@Setter
public class ChartCustomFilterDTO implements Serializable {
    private String fieldId;
    private String term;
    private String value;

    private DatasetTableField field;
}
