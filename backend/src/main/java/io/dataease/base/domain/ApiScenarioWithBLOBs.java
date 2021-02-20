package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ApiScenarioWithBLOBs extends ApiScenario implements Serializable {
    private String scenarioDefinition;

    private String description;

    private static final long serialVersionUID = 1L;
}
