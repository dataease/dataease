package io.dataease.dto.chart;

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
public class ChartFieldCustomFilterDTO implements Serializable {
    private List<ChartCustomFilterItemDTO> filter;
}
