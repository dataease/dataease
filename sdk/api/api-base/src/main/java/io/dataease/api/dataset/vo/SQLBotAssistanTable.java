package io.dataease.api.dataset.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class SQLBotAssistanTable implements Serializable {
    private String name;
    private String comment;
    private String rule;
    private String sql;
    private List<SQLBotAssistantField> fields = new ArrayList<>();

}
