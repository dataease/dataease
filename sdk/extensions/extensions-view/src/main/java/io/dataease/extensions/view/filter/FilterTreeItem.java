package io.dataease.extensions.view.filter;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
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
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fieldId;
    private DatasetTableFieldDTO field;// field object
    private String filterType;// 'logic' or 'enum'
    private String term;//'eq','not_eq','lt','le','gt','ge','in','not in','like','not like','null','not_null','empty','not_empty','between'
    private String value;// 'a'
    private List<String> enumValue;// ['a','b']
    // tree
    private FilterTreeObj subTree;
}
