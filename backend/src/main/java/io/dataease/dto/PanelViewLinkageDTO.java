package io.dataease.dto;

import io.dataease.plugins.common.base.domain.DatasetTableField;
import io.dataease.plugins.common.base.domain.PanelViewLinkage;
import io.dataease.plugins.common.base.domain.PanelViewLinkageField;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: wangjiahao
 * Date: 8/4/21
 * Description:
 */
public class PanelViewLinkageDTO extends PanelViewLinkage {

    @ApiModelProperty("目标视图名称")
    //目标视图名称
    private String targetViewName;
    @ApiModelProperty("联动字段")
    private List<PanelViewLinkageField> linkageFields = new ArrayList<>();

    @ApiModelProperty("目标视图字段")
    private List<DatasetTableField> targetViewFields = new ArrayList<>();
    @ApiModelProperty("表ID")
    private String tableId;



    public PanelViewLinkageDTO() {

    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public List<DatasetTableField> getTargetViewFields() {
        return targetViewFields;
    }

    public void setTargetViewFields(List<DatasetTableField> targetViewFields) {
        this.targetViewFields = targetViewFields;
    }

    public String getTargetViewName() {
        return targetViewName;
    }

    public void setTargetViewName(String targetViewName) {
        this.targetViewName = targetViewName;
    }

    public PanelViewLinkageDTO(boolean linkageActive) {
        super.setLinkageActive(linkageActive);
    }
    public List<PanelViewLinkageField> getLinkageFields() {
        return linkageFields;
    }

    public void setLinkageFields(List<PanelViewLinkageField> linkageFields) {
        this.linkageFields = linkageFields;
    }
}
