package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class AreaMappingGlobal implements Serializable {
    private Long id;

    private String countryCode;

    private String countryName;

    private String provinceName;

    private String provinceCode;

    private String cityName;

    private String cityCode;

    private String countyName;

    private String countyCode;

    private static final long serialVersionUID = 1L;
}