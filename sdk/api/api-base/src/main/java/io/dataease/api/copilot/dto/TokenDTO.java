package io.dataease.api.copilot.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * @Author Junjun
 */
@Data
public class TokenDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String token;
    private Long updateTime;
}
