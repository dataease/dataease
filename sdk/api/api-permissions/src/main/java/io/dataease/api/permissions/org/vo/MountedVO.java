package io.dataease.api.permissions.org.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class MountedVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -7642741925705465785L;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;
    private String name;
    private boolean readOnly = true;
    private List<MountedVO> children;
}
