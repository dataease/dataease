package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class PanelOuterParamsTargetViewInfo implements Serializable {
    private String targetId;

    private String paramsInfoId;

    private String targetViewId;

    private String targetFieldId;

    private String copyFrom;

    private String copyId;

    private static final long serialVersionUID = 1L;
}