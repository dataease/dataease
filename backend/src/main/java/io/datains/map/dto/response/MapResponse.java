package io.datains.map.dto.response;

import io.datains.map.dto.entity.District;
import lombok.Data;

import java.util.List;

@Data
public class MapResponse {

    private String status;

    private String info;

    private String infocode;

    private String count;

    private List<District> districts;
}
