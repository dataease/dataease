package io.dataease.api.dataset.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class SQLBotAssistantField implements Serializable {
    private String name;
    private String comment;
    private String type;

    @JsonIgnore
    private Long fieldId;
    @JsonIgnore
    private String dataeaseName;

    @JsonIgnore
    private boolean needTransform;
    @JsonIgnore
    private boolean needPermission;
    @JsonIgnore
    private Map<String, Object> rowData;
}
