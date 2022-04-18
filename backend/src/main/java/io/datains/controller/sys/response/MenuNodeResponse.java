package io.datains.controller.sys.response;

import io.datains.base.domain.SysMenu;
import lombok.Data;

@Data
public class MenuNodeResponse extends SysMenu {

    private boolean hasChildren;

    private boolean top;
}
