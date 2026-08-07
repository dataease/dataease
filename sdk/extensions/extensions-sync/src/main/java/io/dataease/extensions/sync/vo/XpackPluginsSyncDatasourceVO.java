package io.dataease.extensions.sync.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * @author jianneng
 */
@Data
public class XpackPluginsSyncDatasourceVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String icon;

    private String category;

    private String type;

    private Integer flag;

    private String extraParams;

    private String prefix;

    private String suffix;

    private String driverPath;

    private Integer datasourceRole;

    /**
     * 转换为系统数据源后的类型，为空表示不支持转换
     */
    private String systemDatasourceType;

    private Map<String, String> staticMap;

}
