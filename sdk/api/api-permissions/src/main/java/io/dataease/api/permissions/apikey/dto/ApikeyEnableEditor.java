package io.dataease.api.permissions.apikey.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApikeyEnableEditor implements Serializable {

    private Long id;

    private Boolean enable = false;
}
