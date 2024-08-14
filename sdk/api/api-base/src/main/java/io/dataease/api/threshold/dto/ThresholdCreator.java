package io.dataease.api.threshold.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class ThresholdCreator extends BaseReciDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -4085895087749460947L;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    private String name;

    private Boolean enable = true;

    private Integer rateType = 1;

    private String rateValue;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long resourceId;

    private String resourceType;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long chartId;

    private String chartType;

    private String thresholdRules;

    private Integer msgType = 0;

    private String msgTitle;

    private String msgContent;

    private Boolean repeatSend = true;
}
