package io.dataease.visualization.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 外部参数配置表
 * </p>
 *
 * @author fit2cloud
 * @since 2024-09-09
 */
@TableName("visualization_outer_params_info")
public class VisualizationOuterParamsInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId("params_info_id")
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

    /**
     * 是否必填
     */
    private Boolean required;

    /**
     * 默认值 JSON格式
     */
    private String defaultValue;

    /**
     * 是否启用默认值
     */
    private Boolean enabledDefault;

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

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Boolean getEnabledDefault() {
        return enabledDefault;
    }

    public void setEnabledDefault(Boolean enabledDefault) {
        this.enabledDefault = enabledDefault;
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
        ", required = " + required +
        ", defaultValue = " + defaultValue +
        ", enabledDefault = " + enabledDefault +
        "}";
    }
}
