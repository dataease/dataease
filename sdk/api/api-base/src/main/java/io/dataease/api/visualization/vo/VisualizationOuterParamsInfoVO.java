package io.dataease.api.visualization.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 外部参数配置表
 * </p>
 *
 * @author fit2cloud
 * @since 2024-03-08
 */
@Data
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
    /**
     * 复制来源
     */
    private String copyFrom;

    /**
     * 复制来源ID
     */
    private String copyId;

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
