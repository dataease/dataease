package io.dataease.api.permissions.auth.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SubjectByResourceRequest implements Serializable {

    private Long resourceId;

    private Integer resourceFlag;

    private Integer type;
}
