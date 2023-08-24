package io.dataease.map.dto.entity;


import lombok.Data;

import java.util.List;

@Data
public class District {

    private String adcode;

    private String center;

    private String citycode;

    private String level;

    private String name;

    private String polyline;

    private List<District> districts;
}
