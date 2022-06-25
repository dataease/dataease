package io.dataease.dto.dataset;

import lombok.Data;

import java.util.List;

@Data
public class SqlVariableDetails {
    private String variableName;
    private String alias;
    private List<String> type;
    private String details;
    private String defaultValue;

}
