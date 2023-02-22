package io.dataease.api.permissions.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserDto implements Serializable {

    private Long id;

    private String account;

    private String name;

    private String email;
}
