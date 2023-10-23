package io.dataease.api.xpack.settings.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class XpackAuthenticationVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -1744424881280545811L;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    private String name;

    private boolean enable;
}
