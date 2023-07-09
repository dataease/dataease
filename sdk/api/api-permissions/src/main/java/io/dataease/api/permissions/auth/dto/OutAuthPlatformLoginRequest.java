package io.dataease.api.permissions.auth.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OutAuthPlatformLoginRequest implements Serializable {

    private String account;

    private Integer origin;

    private String email;
}
