package io.dataease.api.xpack.dataFilling.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class DfClearCommitLogRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 807091582312182724L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long formId;

    private String clearType;
}
