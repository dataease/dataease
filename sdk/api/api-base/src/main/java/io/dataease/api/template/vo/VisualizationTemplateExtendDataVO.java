package io.dataease.api.template.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * @author : WangJiaHao
 * @date : 2023/11/13 10:25
 */
@Data
public class VisualizationTemplateExtendDataVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dvId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long viewId;

    private String viewDetails;

    private String copyFrom;

    private String copyId;

}
