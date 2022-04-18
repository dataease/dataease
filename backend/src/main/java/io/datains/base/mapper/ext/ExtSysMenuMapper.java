package io.datains.base.mapper.ext;

import io.datains.base.domain.SysMenu;
import io.datains.base.mapper.ext.query.GridExample;
import io.datains.controller.sys.request.SimpleTreeNode;

import java.util.List;

public interface ExtSysMenuMapper {

    List<SimpleTreeNode> allNodes();

    List<SimpleTreeNode> nodesByExample(GridExample example);

    List<SysMenu> querySysMenu();
}
