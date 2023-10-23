package io.dataease.api.permissions.role.dto;

import io.dataease.model.KeywordRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Data
public class RoleRequest extends KeywordRequest {

    @Serial
    private static final long serialVersionUID = 7354856549096378406L;
    private Long uid;
}
