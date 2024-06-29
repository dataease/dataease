package io.dataease.extensions.view.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author Junjun
 */
@Data
public class DynamicValueDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fieldId;
    private BigDecimal value;
}
