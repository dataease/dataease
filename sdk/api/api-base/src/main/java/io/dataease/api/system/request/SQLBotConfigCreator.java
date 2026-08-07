package io.dataease.api.system.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class SQLBotConfigCreator implements Serializable {

    private String domain;

    private String id;

    private Boolean enabled = false;

    private Boolean valid = false;
}
