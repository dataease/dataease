package io.dataease.extensions.view.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author Junjun
 */
@Data
public class ChartCustomFilterItemDTO implements Serializable {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fieldId;
    private String term;
    private String value;
    private String filterDateFormat;
}
