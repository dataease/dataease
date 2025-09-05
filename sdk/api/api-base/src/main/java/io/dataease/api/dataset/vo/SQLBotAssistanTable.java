package io.dataease.api.dataset.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class SQLBotAssistanTable implements Serializable {
    private String name;
    private String comment;
    private String rule;
    private String sql;
    private List<SQLBotAssistantField> fields = new ArrayList<>();

    @JsonIgnore
    private Long datasetGroupId;
    @JsonIgnore
    private boolean needTransform;
    @JsonIgnore
    private boolean needPermission;
    @JsonIgnore
    private Map<String, Object> rowData;

}
