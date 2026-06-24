package io.dataease.api.permissions.auth.dto;

import io.dataease.model.KeywordRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class SubjectRequest extends KeywordRequest implements Serializable {

    private boolean system;

    private int type;

    private boolean lazy = true;

    private Long pid;
}
