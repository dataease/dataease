package io.dataease.plugins.common.base.domain;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PanelTemplateWithBLOBs extends PanelTemplate implements Serializable {
    @ApiModelProperty("快照")
    private String snapshot;
    @ApiModelProperty("模版样式")
    private String templateStyle;
    @ApiModelProperty("模版数据")
    private String templateData;
    @ApiModelProperty("动态数据")
    private String dynamicData;

    private static final long serialVersionUID = 1L;
}