package io.dataease.controller.sys.request;

import io.dataease.plugins.common.base.domain.SysMenu;
import lombok.Data;

@Data
public class MenuCreateRequest extends SysMenu {

    private boolean top;
}
