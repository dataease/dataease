package io.dataease.api.permissions.embedded.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class EmbeddedOrigin implements Serializable {

    private String token;

    private String origin;
}
