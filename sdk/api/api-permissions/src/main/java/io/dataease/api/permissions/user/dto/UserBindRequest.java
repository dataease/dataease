package io.dataease.api.permissions.user.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserBindRequest implements Serializable {

    private Integer origin;

    private String sub;
}
