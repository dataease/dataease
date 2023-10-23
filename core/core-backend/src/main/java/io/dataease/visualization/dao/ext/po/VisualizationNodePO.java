package io.dataease.visualization.dao.ext.po;

import lombok.Data;

import java.io.Serializable;


@Data
public class VisualizationNodePO implements Serializable {

    private Long id;
    private String name;
    private Long pid;
    private String nodeType;
}
