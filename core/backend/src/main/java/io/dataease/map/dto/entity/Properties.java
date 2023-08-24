package io.dataease.map.dto.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
public class Properties implements Serializable {

    private String adcode;

    private String name;

    private List<Double> center;

    private List<Double> centroid;

    private Integer childrenNum;

    private String level;

    private Parent parent;

    private Integer subFeatureIndex;

    private List<Double> acroutes;
}
