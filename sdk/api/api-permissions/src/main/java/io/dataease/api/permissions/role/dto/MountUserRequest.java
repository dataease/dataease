package io.dataease.api.permissions.role.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class MountUserRequest implements Serializable {

    private Long rid;
    private List<Long> uids;
}
