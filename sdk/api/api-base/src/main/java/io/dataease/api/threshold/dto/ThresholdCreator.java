package io.dataease.api.threshold.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class ThresholdCreator extends BaseReciDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -4085895087749460947L;

    private Long id;

    private String name;

    private Boolean enable = true;

    private Integer rateType = 1;

    private Integer rateValue = 1;

    private Long rateTime;

    private Long resourceId;

    private String resourceType;

    private Long chartId;

    private String chartType;

    private String thresholdRules;

    private Integer msgType = 0;

    private String msgTitle;

    private String msgContent;

    private Boolean repeat = true;
}
