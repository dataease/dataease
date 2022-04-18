package io.datains.map.dto.response;

import io.datains.map.dto.entity.Feature;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
public class MapResultDto implements Serializable {

    private String type;

    private List<Feature> features;
}
