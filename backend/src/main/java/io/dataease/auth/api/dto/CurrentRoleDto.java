package io.dataease.auth.api.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CurrentRoleDto implements Serializable {

    private Long id;

    private String code;

    private String name;
}
