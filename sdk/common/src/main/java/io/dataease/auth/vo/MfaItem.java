package io.dataease.auth.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class MfaItem implements Serializable {
    @Serial
    private static final long serialVersionUID = 6647534143991435022L;

    private boolean enabled;

    private boolean ready;

    private String challenge;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long uid;
}
