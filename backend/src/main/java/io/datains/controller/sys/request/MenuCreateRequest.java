package io.datains.controller.sys.request;

import io.datains.base.domain.SysMenu;
import lombok.Data;

@Data
public class MenuCreateRequest extends SysMenu {

    private boolean top;
}
