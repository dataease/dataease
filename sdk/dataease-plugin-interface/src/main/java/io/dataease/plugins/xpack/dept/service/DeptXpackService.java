package io.dataease.plugins.xpack.dept.service;

import io.dataease.plugins.common.service.PluginMenuService;
import io.dataease.plugins.xpack.dept.dto.request.*;
import io.dataease.plugins.xpack.dept.dto.response.DeptUserItemDTO;
import io.dataease.plugins.xpack.dept.dto.response.XpackDeptTreeNode;
import io.dataease.plugins.xpack.dept.dto.response.XpackSysDept;

import java.util.List;

public abstract class DeptXpackService extends PluginMenuService {

    public abstract List<XpackSysDept> nodesByPid(Long pid);

    public abstract List<XpackSysDept> nodesTreeByCondition(XpackDeptGridRequest request);

    public abstract List<XpackDeptTreeNode> searchTree(Long deptId);

    public abstract int add(XpackCreateDept xpackCreateDept);

    public abstract int update(XpackCreateDept xpackCreateDept);

    public abstract int batchDelete(List<XpackDeleteDept> requests);

    public abstract void move(XpackMoveDept xpackMoveDept);

    public abstract void bindUser(XpackDeptBindRequest request);

    public abstract void unBindUsers(XpackDeptBindRequest request);

    public abstract List<DeptUserItemDTO> queryBinded(XpackDeptUserRequest request, boolean isPage);


}
