package io.dataease.api.dataset.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class DataSQLBotAssistantVO implements Serializable {

    private String name;

    private String type;

    private String host;

    private int port;

    private String dataBase;

    private String extraParams;

    private String user;

    private String password;

    private String schema;

    private String comment;

    private List<SQLBotAssistanTable> tables = new ArrayList<>();

}
