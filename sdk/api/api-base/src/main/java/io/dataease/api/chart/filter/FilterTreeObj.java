package io.dataease.api.chart.filter;

import lombok.Data;

import java.util.List;


/**
 * @Author Junjun
 */
@Data
public class FilterTreeObj {
    private String logic;
    private List<FilterTreeItem> items;
}
