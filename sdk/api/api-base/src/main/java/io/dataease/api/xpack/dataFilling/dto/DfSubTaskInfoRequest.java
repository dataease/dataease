package io.dataease.api.xpack.dataFilling.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class DfSubTaskInfoRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 3478936079850972546L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long taskId;

}
