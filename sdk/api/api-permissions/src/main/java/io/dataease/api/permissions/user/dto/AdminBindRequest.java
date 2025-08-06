package io.dataease.api.permissions.user.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AdminBindRequest extends UserBindRequest {
    private Long uid;
}
