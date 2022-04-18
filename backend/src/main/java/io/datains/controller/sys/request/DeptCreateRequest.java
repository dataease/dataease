package io.datains.controller.sys.request;

import io.datains.base.domain.SysDept;
import lombok.Data;

@Data
public class DeptCreateRequest extends SysDept {

    private boolean top;

}
