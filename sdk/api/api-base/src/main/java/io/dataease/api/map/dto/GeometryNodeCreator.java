package io.dataease.api.map.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class GeometryNodeCreator implements Serializable {

    private String code;

    private String name;

    private String pid;
}
