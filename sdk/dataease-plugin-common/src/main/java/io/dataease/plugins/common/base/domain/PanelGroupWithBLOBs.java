package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PanelGroupWithBLOBs extends PanelGroup implements Serializable {
    private String panelStyle;

    private String panelData;

    private static final long serialVersionUID = 1L;
}