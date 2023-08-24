package io.dataease.map.dto.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class MapRequest implements Serializable {

    private String keywords;

    private Integer subdistrict;

    private String extensions;

    private String key;

    private Integer page = 1;
}
