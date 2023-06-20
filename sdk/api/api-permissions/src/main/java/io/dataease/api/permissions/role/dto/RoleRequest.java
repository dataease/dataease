package io.dataease.api.permissions.role.dto;

import io.dataease.model.KeywordRequest;
import lombok.Data;

@Data
public class RoleRequest extends KeywordRequest {

    private Long uid;
}
