package io.dataease.api.sync.datasource.dto;

import lombok.Data;

@Data
public class GetDatasourceRequest extends SyncDatasourceDTO {

    /**
     * 查询sql
     */
    private String query;
    /**
     * 表名
     */
    private String table;
    /**
     * 表格的抽取数据方式
     */
    private boolean tableExtract;
    /**
     * 不为空时，获取源数据库表字段，将转换为doris的数据类型
     */
    private String targetDbType;

}
