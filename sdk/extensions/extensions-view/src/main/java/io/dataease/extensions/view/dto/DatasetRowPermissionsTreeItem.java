package io.dataease.extensions.view.dto;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

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

    public String getValue() {
        if (StringUtils.isNotEmpty(timeValue)) {
            return timeValue;
        }
        return value;
    }

    private String value;// 'a'
    private String timeValue;// 'a'
    private List<String> enumValue;// ['a','b']

    private String timeType; // 时间细粒度

    private DatasetRowPermissionsTreeObj subTree;
    private static final long serialVersionUID = 1L;
}
