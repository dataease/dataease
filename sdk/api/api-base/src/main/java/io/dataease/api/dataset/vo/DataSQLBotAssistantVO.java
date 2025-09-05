package io.dataease.api.dataset.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class DataSQLBotAssistantVO implements Serializable {

    private String name;

    private String type;

    private String host;

    private Integer port;

    private String dataBase;

    private String extraParams;

    private String user;

    private String password;

    private String schema;

    private String comment;

    private List<SQLBotAssistanTable> tables = new ArrayList<>();

    @JsonIgnore
    private Map<String, Object> rowData;

}
