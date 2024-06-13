package io.dataease.extensions.view.filter;

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
