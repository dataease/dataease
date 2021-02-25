package io.dataease.controller.sys.response;

import io.dataease.base.domain.SysRole;
import lombok.Data;

import java.util.List;

@Data
public class RoleNodeResponse extends SysRole {

    private List<Long> menuIds;

    private List<Long> dataIds;
}
