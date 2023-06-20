package io.dataease.api.permissions.user.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class CurUserVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1190164294672439979L;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;
    private String name;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long oid;
    private String language;
}
