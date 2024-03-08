package io.dataease.api.visualization.vo;

import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

/**
 * <p>
 * 外部参数配置表
 * </p>
 *
 * @author fit2cloud
 * @since 2024-03-08
 */
public class VisualizationOuterParamsInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private String paramsInfoId;

    /**
     * visualization_outer_params 表的 ID
     */
    private String paramsId;

    /**
     * 参数名
     */
    private String paramName;

    /**
     * 是否启用
     */
    private Boolean checked;

    /**
     * 复制来源
     */
    private String copyFrom;

    /**
     * 复制来源ID
     */
    private String copyId;

    public String getParamsInfoId() {
        return paramsInfoId;
    }

    public void setParamsInfoId(String paramsInfoId) {
        this.paramsInfoId = paramsInfoId;
    }

    public String getParamsId() {
        return paramsId;
    }

    public void setParamsId(String paramsId) {
        this.paramsId = paramsId;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
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
        return "VisualizationOuterParamsInfo{" +
        "paramsInfoId = " + paramsInfoId +
        ", paramsId = " + paramsId +
        ", paramName = " + paramName +
        ", checked = " + checked +
        ", copyFrom = " + copyFrom +
        ", copyId = " + copyId +
        "}";
    }
}
