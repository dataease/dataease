package io.dataease.api.chart.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ViewDetailField implements Serializable {

    private String name;

    private String dataeaseName;

    private Integer deType;
}
