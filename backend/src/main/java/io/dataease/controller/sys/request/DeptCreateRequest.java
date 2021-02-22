package io.dataease.controller.sys.request;

import io.dataease.base.domain.SysDept;
import lombok.Data;

@Data
public class DeptCreateRequest extends SysDept {

    private boolean top;

}
