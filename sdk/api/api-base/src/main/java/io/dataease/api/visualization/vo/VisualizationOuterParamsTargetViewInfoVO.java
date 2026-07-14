package io.dataease.api.visualization.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 外部参数联动视图字段信息表
 * </p>
 *
 * @author fit2cloud
 * @since 2024-03-08
 */
@Data
public class VisualizationOuterParamsTargetViewInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetId;

    /**
     * visualization_outer_params_info 表的 ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long paramsInfoId;

    /**
     * 联动视图ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetViewId;

    /**
     * 联动数据集id/联动过滤组件id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetDsId;

    /**
     * 联动字段ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private String targetFieldId;

    /**
     * 复制来源
     */
    private String copyFrom;

    /**
     * 复制来源ID
     */
    private String copyId;

    private String matchMode;

}
