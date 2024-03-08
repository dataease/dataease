package io.dataease.api.visualization.vo;

import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

/**
 * <p>
 * 外部参数关联关系表
 * </p>
 *
 * @author fit2cloud
 * @since 2024-03-08
 */
public class VisualizationOuterParamsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private String paramsId;

    /**
     * 可视化资源ID
     */
    private String visualizationId;

    /**
     * 是否启用外部参数标识（1-是，0-否）
     */
    private Boolean checked;

    /**
     * 备注
     */
    private String remark;

    /**
     * 复制来源
     */
    private String copyFrom;

    /**
     * 复制来源ID
     */
    private String copyId;

    public String getParamsId() {
        return paramsId;
    }

    public void setParamsId(String paramsId) {
        this.paramsId = paramsId;
    }

    public String getVisualizationId() {
        return visualizationId;
    }

    public void setVisualizationId(String visualizationId) {
        this.visualizationId = visualizationId;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCopyFrom() {
        return copyFrom;
    }

    public void setCopyFrom(String copyFrom) {
        this.copyFrom = copyFrom;
    }

    public String getCopyId() {
        return copyId;
    }

    public void setCopyId(String copyId) {
        this.copyId = copyId;
    }

    @Override
    public String toString() {
        return "VisualizationOuterParams{" +
        "paramsId = " + paramsId +
        ", visualizationId = " + visualizationId +
        ", checked = " + checked +
        ", remark = " + remark +
        ", copyFrom = " + copyFrom +
        ", copyId = " + copyId +
        "}";
    }
}
