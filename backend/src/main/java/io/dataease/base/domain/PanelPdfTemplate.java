package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class PanelPdfTemplate implements Serializable {
    private String id;

    private String name;

    private Long createTime;

    private String createUser;

    private Integer sort;

    private String templateContent;

    private static final long serialVersionUID = 1L;
}