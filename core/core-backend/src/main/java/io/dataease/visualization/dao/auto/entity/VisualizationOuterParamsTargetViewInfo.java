package io.dataease.visualization.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 外部参数联动视图字段信息表
 * </p>
 *
 * @author fit2cloud
 * @since 2024-08-21
 */
@TableName("visualization_outer_params_target_view_info")
public class VisualizationOuterParamsTargetViewInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId("target_id")
    private String targetId;

    /**
     * visualization_outer_params_info 表的 ID
     */
    private String paramsInfoId;

    /**
     * 联动视图ID/联动过滤项ID
     */
    private String targetViewId;

    /**
     * 联动数据集id/联动过滤组件id
     */
    private String targetDsId;

    /**
     * 联动字段ID
     */
    private String targetFieldId;

    /**
     * 复制来源
     */
    private String copyFrom;

    /**
     * 复制来源ID
     */
    private String copyId;

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getParamsInfoId() {
        return paramsInfoId;
    }

    public void setParamsInfoId(String paramsInfoId) {
        this.paramsInfoId = paramsInfoId;
    }

    public String getTargetViewId() {
        return targetViewId;
    }

    public void setTargetViewId(String targetViewId) {
        this.targetViewId = targetViewId;
    }

    public String getTargetDsId() {
        return targetDsId;
    }

    public void setTargetDsId(String targetDsId) {
        this.targetDsId = targetDsId;
    }

    public String getTargetFieldId() {
        return targetFieldId;
    }

    public void setTargetFieldId(String targetFieldId) {
        this.targetFieldId = targetFieldId;
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
        return "VisualizationOuterParamsTargetViewInfo{" +
        "targetId = " + targetId +
        ", paramsInfoId = " + paramsInfoId +
        ", targetViewId = " + targetViewId +
        ", targetDsId = " + targetDsId +
        ", targetFieldId = " + targetFieldId +
        ", copyFrom = " + copyFrom +
        ", copyId = " + copyId +
        "}";
    }
}
