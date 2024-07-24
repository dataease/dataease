package io.dataease.extensions.datasource.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
public class XpackPluginsDatasourceVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String icon;

    private String category;

    private String type;

    private String extraParams;

    private String prefix;

    private String suffix;

    private String driverPath;

    private Map<String, String> staticMap;

}
