package io.dataease.plugins.common.dto.chart;

import io.dataease.plugins.common.base.domain.DatasetTableField;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @Author gin
 * @Date 2021/7/20 11:43 上午
 */
@Getter
@Setter
public class ChartFieldCustomFilterDTO extends ChartViewFieldBaseDTO implements Serializable {
    private List<ChartCustomFilterItemDTO> filter;

    private DatasetTableField field;

    private List<String> enumCheckField;
}
