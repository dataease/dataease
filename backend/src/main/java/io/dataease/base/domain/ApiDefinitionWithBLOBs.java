package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ApiDefinitionWithBLOBs extends ApiDefinition implements Serializable {
    private String description;

    private String request;

    private String response;

    private static final long serialVersionUID = 1L;
}
