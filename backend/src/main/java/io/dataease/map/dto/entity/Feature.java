package io.dataease.map.dto.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class Feature implements Serializable {

    private String type;

    private Properties properties;

    private Geometry geometry;
}
