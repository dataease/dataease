package io.dataease.api.chart.filter;

import io.dataease.dto.dataset.DatasetTableFieldDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author Junjun
 */
@Data
public class FilterTreeItem implements Serializable {
    private String type;// 'item' or 'tree'
    // item
    private Long fieldId;
    private DatasetTableFieldDTO field;// field object
    private String filterType;// 'logic' or 'enum'
    private String term;//'eq','not_eq','lt','le','gt','ge','in','not in','like','not like','null','not_null','empty','not_empty','between'
    private String value;// 'a'
    private List<String> enumValue;// ['a','b']
    // tree
    private FilterTreeObj subTree;
}
