package io.dataease.api.permissions.auth.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PermissionSubjectRequest extends SubjectRequest {
    private Long datasetId;
}
