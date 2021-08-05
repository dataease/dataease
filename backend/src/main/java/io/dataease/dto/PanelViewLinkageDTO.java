package io.dataease.dto;

import io.dataease.base.domain.DatasetTableField;
import io.dataease.base.domain.PanelViewLinkage;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: wangjiahao
 * Date: 8/4/21
 * Description:
 */
public class PanelViewLinkageDTO extends PanelViewLinkage {

    //目标视图名称
    private String targetViewName;

    //关联状态
    private boolean linkageActive = false;

    private List<PanelViewLinkageFieldDTO> linkageFields = new ArrayList<>();


    private List<DatasetTableField> targetViewField = new ArrayList<>();



    public PanelViewLinkageDTO() {

    }

    public List<DatasetTableField> getTargetViewField() {
        return targetViewField;
    }

    public void setTargetViewField(List<DatasetTableField> targetViewField) {
        this.targetViewField = targetViewField;
    }

    public String getTargetViewName() {
        return targetViewName;
    }

    public void setTargetViewName(String targetViewName) {
        this.targetViewName = targetViewName;
    }

    public PanelViewLinkageDTO(boolean linkageActive) {
        this.linkageActive = linkageActive;
    }

    public boolean isLinkageActive() {
        return linkageActive;
    }

    public void setLinkageActive(boolean linkageActive) {
        this.linkageActive = linkageActive;
    }

    public List<PanelViewLinkageFieldDTO> getLinkageFields() {
        return linkageFields;
    }

    public void setLinkageFields(List<PanelViewLinkageFieldDTO> linkageFields) {
        this.linkageFields = linkageFields;
    }
}
