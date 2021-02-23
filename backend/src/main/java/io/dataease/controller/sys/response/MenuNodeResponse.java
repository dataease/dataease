package io.dataease.controller.sys.response;

import io.dataease.base.domain.SysMenu;
import lombok.Data;

@Data
public class MenuNodeResponse extends SysMenu {

    private boolean hasChildren;

    private boolean top;
}
