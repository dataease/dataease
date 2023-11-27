package io.dataease.plugins.common.dto.sqlObj;

import lombok.Builder;
import lombok.Data;

/**
 * @Author gin
 * @Date 2021/7/9 12:12 下午
 */
@Data
@Builder
public class SQLObj {
    private String tableName;
    private String tableAlias;

    private String fieldName;
    private String fieldOriginName;
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
