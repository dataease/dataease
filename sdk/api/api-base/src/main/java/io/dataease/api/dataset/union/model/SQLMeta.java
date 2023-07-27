package io.dataease.api.dataset.union.model;

import lombok.Data;

import java.util.List;

/**
 * @Author Junjun
 */
@Data
public class SQLMeta {

    private SQLObj table;

    private List<SQLObj> xFields;

    private List<String> xWheres;

    private List<SQLObj> xOrders;

    private List<SQLObj> yFields;

    private List<String> yWheres;

    private List<SQLObj> yOrders;

    /**
     * 视图过滤字段
     */
    private String customWheres;

    /**
     * 仪表板过滤字段
     */
    private String extWheres;

    /**
     * 行权限过滤
     */
    private String whereTrees;

}
