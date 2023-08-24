package io.dataease.plugins.common.request.permission;

import io.dataease.plugins.common.base.domain.DatasetTableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DatasetRowPermissionsTreeItem implements Serializable {
    @ApiModelProperty("树节点类型：item or tree")
    private String type;// 'item' or 'tree'
    // item
    @ApiModelProperty("字段ID")
    private String fieldId;
    @ApiModelProperty("字段对象")
    private DatasetTableField field;// field object
    @ApiModelProperty("筛选方式：logic or enum")
    private String filterType;// 'logic' or 'enum'
    @ApiModelProperty("条件：'eq','not_eq','lt','le','gt','ge','in','not in','like','not like','null','not_null','empty','not_empty','between'")
    private String term;//'eq','not_eq','lt','le','gt','ge','in','not in','like','not like','null','not_null','empty','not_empty','between'
    @ApiModelProperty("值")
    private String value;// 'a'
    @ApiModelProperty("枚举值")
    private List<String> enumValue;// ['a','b']
    // tree
    @ApiModelProperty("子节点树，仅当type='tree'时有效")
    private DatasetRowPermissionsTreeObj subTree;

    private static final long serialVersionUID = 1L;
}
