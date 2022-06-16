package io.dataease.dto.dataset;

import lombok.Data;

@Data
public class SqlVariableDetails {
    private String variableName;
    private String alias;
    private String type;
    private String details;
    private String defaultValue;

}
