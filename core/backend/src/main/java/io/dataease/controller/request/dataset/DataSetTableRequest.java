package io.dataease.controller.request.dataset;

import io.dataease.dto.dataset.ExcelSheetData;
import io.dataease.plugins.common.base.domain.DatasetTable;
import io.dataease.plugins.common.dto.datasource.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

/**
 * @Author gin
 * @Date 2021/2/23 3:06 下午
 */
@Setter
@Getter
public class DataSetTableRequest extends DatasetTable {
    @ApiModelProperty("排序")
    private String sort;
    @ApiModelProperty("表名集合")
    private List<String> tableNames;
    @ApiModelProperty("行数")
    private String row = "1000";
    @ApiModelProperty("用户ID")
    private String userId;
    @ApiModelProperty("同步类型")
    private String syncType;
    @ApiModelProperty("编辑类型")
    private Integer editType;
    @ApiModelProperty("是否重命名")
    private Boolean isRename;
    @ApiModelProperty("类型过滤条件集合")
    private List<String> typeFilter;
    @ApiModelProperty("字段集合")
    private List<TableField> fields;
    @ApiModelProperty("excel sheet集合")
    private List<ExcelSheetData> sheets;
    @ApiModelProperty("是否合并sheet")
    private boolean mergeSheet = false;
    private boolean previewForTask = false;
    @ApiModelProperty("操作来源")
    private String optFrom;
}
