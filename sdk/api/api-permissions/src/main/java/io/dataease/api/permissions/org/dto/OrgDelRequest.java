package io.dataease.api.permissions.org.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrgDelRequest implements Serializable {

    private Long id;

    private boolean delResource;
}
