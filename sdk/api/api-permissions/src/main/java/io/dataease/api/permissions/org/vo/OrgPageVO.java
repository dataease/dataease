package io.dataease.api.permissions.org.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class OrgPageVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -7788232223396601785L;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    private String name;

    private Long createTime;

    private boolean readOnly = true;

    private List<OrgPageVO> children;
}
