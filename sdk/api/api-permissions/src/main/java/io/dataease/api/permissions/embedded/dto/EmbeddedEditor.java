package io.dataease.api.permissions.embedded.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class EmbeddedEditor implements Serializable {

    private Long id;

    private String name;

    private String domain;
}
