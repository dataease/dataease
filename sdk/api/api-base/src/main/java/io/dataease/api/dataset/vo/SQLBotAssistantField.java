package io.dataease.api.dataset.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SQLBotAssistantField implements Serializable {
    private String name;
    private String comment;
    private String type;
}
