package io.dataease.menu.bo;

import io.dataease.menu.dao.auto.entity.CoreMenu;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MenuTreeNode extends CoreMenu {

    private List<MenuTreeNode> children = new ArrayList<>();
}
