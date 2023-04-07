package io.dataease.api.dataset.union.model;

import lombok.Builder;
import lombok.Data;

/**
 * @Author gin
 * @Date 2023/3/23 16:12 下午
 */
@Data
@Builder
public class SQLObj {
    private String tableSchema;
    private String tableName;
    private String tableAlias;

    private String fieldName;
    private String fieldAlias;

    private String groupField;
    private String groupAlias;

    private String orderField;
    private String orderAlias;
    private String orderDirection;

    private String whereField;
    private String whereAlias;
    private String whereTermAndValue;

    private String limitFiled;
}
