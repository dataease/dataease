package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class AreaMapping implements Serializable {
    private Long id;

    private String provinceName;

    private String provinceCode;

    private String cityName;

    private String cityCode;

    private String countyName;

    private String countyCode;

    private static final long serialVersionUID = 1L;
}