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

    private Integer flag;

    private String extraParams;

    private String prefix;

    private String suffix;

    /** 数据集 SQL 助手可复用的基础方言。插件在验证兼容后声明，缺省表示尚未适配。 */
    private String aiSqlBaseDialect;

    private String driverPath;

    private Map<String, String> staticMap;

}
