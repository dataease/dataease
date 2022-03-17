package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class PanelOuterParams implements Serializable {
    private String paramsId;

    private String panelId;

    private Boolean checked;

    private String remark;

    private String copyFrom;

    private String copyId;

    private static final long serialVersionUID = 1L;
}