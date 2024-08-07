package io.dataease.api.xpack.dataFilling.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class DfUserTaskRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 3930338226463461223L;

    private String type;

    private String taskName;
}
