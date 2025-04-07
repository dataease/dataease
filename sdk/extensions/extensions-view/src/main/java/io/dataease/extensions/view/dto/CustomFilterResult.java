package io.dataease.extensions.view.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomFilterResult {
    private List<ChartExtFilterDTO> filterList;
    private Map<String, Object> context;
    private boolean isDrill;// 组合图右轴判断是否下钻字段，其余地方没有用

    public CustomFilterResult(List<ChartExtFilterDTO> filterList, Map<String, Object> context) {
        this.filterList = filterList;
        this.context = context;
    }
}
