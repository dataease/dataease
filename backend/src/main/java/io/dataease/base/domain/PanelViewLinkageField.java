package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class PanelViewLinkageField implements Serializable {
    private String id;

    private String linkageId;

    private String sourceField;

    private String targetField;

    private Long updateTime;

    private static final long serialVersionUID = 1L;
}