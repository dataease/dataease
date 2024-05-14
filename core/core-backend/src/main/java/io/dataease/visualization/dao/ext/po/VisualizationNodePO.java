package io.dataease.visualization.dao.ext.po;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;


@Data
public class VisualizationNodePO implements Serializable {

    private Long id;
    private String name;
    private Long pid;
    private String nodeType;
    @Schema(description = "额外标识")
    private int extraFlag;

}
