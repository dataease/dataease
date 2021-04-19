package io.dataease.controller.request.chart;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author gin
 * @Date 2021/4/19 11:29 上午
 */
@Getter
@Setter
public class ChartExtRequest {
    private List<ChartExtFilterRequest> filter;
}
