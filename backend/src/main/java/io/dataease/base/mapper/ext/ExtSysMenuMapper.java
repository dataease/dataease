package io.dataease.base.mapper.ext;

import io.dataease.base.domain.SysMenu;
import io.dataease.base.mapper.ext.query.GridExample;
import io.dataease.controller.sys.request.SimpleTreeNode;

import java.util.List;

public interface ExtSysMenuMapper {

    List<SimpleTreeNode> allNodes();

    List<SimpleTreeNode> nodesByExample(GridExample example);

    List<SysMenu> querySysMenu();
}
