package io.dataease.extensions.datasource.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @Author Junjun
 */
@Data
public class SQLMeta {

    private String chartType;

    private SQLObj table;

    /**
     * SQL片段占位符
     */
    private String tableDialect;

    private List<SQLObj> xFields;

    private Map<String, String> xFieldsDialect;

    private List<String> xWheres;

    private List<SQLObj> xOrders;

    private List<SQLObj> yFields;

    private Map<String, String> yFieldsDialect;

    private List<String> yWheres;

    private List<SQLObj> yOrders;

    /**
     * 图表过滤字段
     */
    private String customWheres;

    private Map<String, String> customWheresDialect;

    /**
     * 仪表板过滤字段
     */
    private String extWheres;

    private Map<String, String> extWheresDialect;

    /**
     * 行权限过滤
     */
    private String whereTrees;

    private Map<String, String> whereTreesDialect;

}
