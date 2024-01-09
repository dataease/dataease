package io.dataease.api.permissions.auth.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ResourceNodeVO implements Serializable {

    private Long id;

    private String name;
}
