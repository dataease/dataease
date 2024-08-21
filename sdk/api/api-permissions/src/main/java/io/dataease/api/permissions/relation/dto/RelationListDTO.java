package io.dataease.api.permissions.relation.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author Junjun
 */
@Data
public class RelationListDTO implements Serializable {
    private Long dsId;
    private String dsName;
    private String dsCreator;
    private Long dsUpdateTime;

    private Long datasetId;
    private String datasetName;
    private String datasetCreator;
    private Long datasetUpdateTime;

    private Long dashboardId;
    private String dashboardName;
    private String dashboardCreator;
    private Long dashboardUpdateTime;

    private Long dvId;
    private String dvName;
    private String dvCreator;
    private Long dvUpdateTime;

    private String type;
}
