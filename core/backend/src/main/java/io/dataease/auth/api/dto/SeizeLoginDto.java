package io.dataease.auth.api.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SeizeLoginDto implements Serializable {

    private static final long serialVersionUID = -3318473577764636483L;

    private String token;
}
