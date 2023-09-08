package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class VAuthModelWithBLOBs extends VAuthModel implements Serializable {
    private String name;

    private String label;

    private static final long serialVersionUID = 1L;
}