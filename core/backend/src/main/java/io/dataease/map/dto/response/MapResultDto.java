package io.dataease.map.dto.response;

import io.dataease.map.dto.entity.Feature;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
public class MapResultDto implements Serializable {

    private String type;

    private List<Feature> features;
}
