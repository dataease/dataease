package io.dataease.map.dto.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Geometry implements Serializable {

    private String type;

    // 多个面构成
    // point一维数组 polyline二维数组 polygon三维数组 MultiPolygon四维数组
    private List<List<List<List<Double>>>> coordinates;

}
