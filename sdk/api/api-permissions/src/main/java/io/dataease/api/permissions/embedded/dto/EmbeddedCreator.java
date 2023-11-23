package io.dataease.api.permissions.embedded.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class EmbeddedCreator implements Serializable {

    private String name;

    private String domain;
}
