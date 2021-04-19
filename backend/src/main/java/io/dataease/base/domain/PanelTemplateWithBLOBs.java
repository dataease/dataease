package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PanelTemplateWithBLOBs extends PanelTemplate implements Serializable {
    private String snapshot;

    private String templateStyle;

    private String templateData;

    private String dynamicData;

    private static final long serialVersionUID = 1L;
}