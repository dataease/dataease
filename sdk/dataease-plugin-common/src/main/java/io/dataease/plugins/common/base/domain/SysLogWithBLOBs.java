package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SysLogWithBLOBs extends SysLog implements Serializable {
    private String position;

    private String remark;

    private static final long serialVersionUID = 1L;
}