package io.dataease.api.permissions.auth.dto;

import io.dataease.api.permissions.user.vo.UserReciVO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ResourcePermissionRequest implements Serializable {

    private List<Long> resourceIds;

    private Integer resourceType;

    private List<UserReciVO> targets;
}
