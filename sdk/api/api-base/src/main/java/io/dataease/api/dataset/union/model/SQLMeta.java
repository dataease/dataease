package io.dataease.api.dataset.union.model;

import lombok.Builder;
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

    private List<SQLObj> xFieldsExt;

    private List<String> xWheresExt;

    private List<SQLObj> xOrdersExt;

    private List<SQLObj> yFields;

    private List<String> yWheres;

    private List<SQLObj> yOrders;

    private List<SQLObj> yFieldsExt;

    private List<String> yWheresExt;

    private List<SQLObj> yOrdersExt;

    /**
     * 堆叠项
     */
    private List<SQLObj> extStack;

    /**
     * 气泡大小
     */
    private List<SQLObj> extBubble;

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
