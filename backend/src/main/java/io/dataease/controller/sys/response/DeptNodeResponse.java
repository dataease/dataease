package io.dataease.controller.sys.response;

import io.dataease.base.domain.SysDept;
import lombok.Data;

import java.util.List;

@Data
public class DeptNodeResponse extends SysDept {
    private boolean hasChildren;

    private boolean leaf;

    private boolean top;

}
