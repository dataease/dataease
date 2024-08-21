package io.dataease.api.threshold.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ThresholdGridVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 3729780170978191092L;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    private String name;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long resourceId;

    private String resourceType;

    private String resourceName;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long chartId;

    private String chartType;

    private String chartName;

    private Boolean status;

    private Boolean enable;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long creator;

    private String createName;

    private Long createTime;
}
