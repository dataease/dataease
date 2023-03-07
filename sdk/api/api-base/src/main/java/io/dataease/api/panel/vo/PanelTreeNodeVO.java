package io.dataease.api.panel.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class PanelTreeNodeVO implements Serializable {

    private Long id;

    private String name;

    private String type;
}
