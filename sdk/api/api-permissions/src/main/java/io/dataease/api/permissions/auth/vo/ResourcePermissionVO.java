package io.dataease.api.permissions.auth.vo;

import io.dataease.api.permissions.user.vo.UserReciVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class ResourcePermissionVO extends UserReciVO implements Serializable {
    private Long resourceId;
    private boolean enable;
}
