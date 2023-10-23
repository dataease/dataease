package io.dataease.api.permissions.dataset.dto;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.dataease.dto.dataset.DatasetTableFieldDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DatasetRowPermissionsTreeItem implements Serializable {
    private String type;// 'item' or 'tree'
    // item

    @JsonSerialize(using = ToStringSerializer.class)
    private Long fieldId;

    private DatasetTableFieldDTO field;// field object

    private String filterType;// 'logic' or 'enum'
    private String term;//'eq','not_eq','lt','le','gt','ge','in','not in','like','not like','null','not_null','empty','not_empty','between
    private String value;// 'a'
    private List<String> enumValue;// ['a','b']

    private DatasetRowPermissionsTreeObj subTree;
    private static final long serialVersionUID = 1L;
}
