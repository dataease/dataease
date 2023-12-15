package io.dataease.api.sync.datasource.vo.catalog.doris;

import java.util.Map;

/**
 * doris模版类
 * @author fit2cloud
 * @date 2023/9/1 10:19
 **/
public interface DorisCatalogTemplate {
    /**
     * doris 建语句表模版
     * table_identifier 数据库及表名 databaseName.tableName
     * column_definition 列属性
     * engine_type 默认olap
     * key_columns 固定数据存储模型为Unique
     * table_comment 表备注
     * partition_type 分区方式Range、List
     * partition_column 分区字段
     * partition_info 分区值
     * distribution_columns 分布存储字段
     * distribution_bucket 分桶数量，默认10
     * properties 表参数
     */
    String DEFAULT_CREATE_TEMPLATE =
            "CREATE TABLE ${table_identifier}\n"
            + "(\n"
            + "${column_definition}\n"
            + ")\n"
            + "ENGINE = ${engine_type}\n"
            + "UNIQUE KEY (${key_columns})\n"
            + "COMMENT ${table_comment}\n"
            + "${partition}\n"
            + "DISTRIBUTED BY HASH (${distribution_columns}) BUCKETS ${distribution_bucket}\n"
            //+ "DISTRIBUTED BY RANDOM BUCKETS ${distribution_bucket}\n"
            + "PROPERTIES (\n"
            + "${properties}\n"
            + ")\n";

    /**
     * doris特性配置 properties配置
     * - replication_allocation: 指定副本数。BE 节点数量，默认副本数为3,我们只有一个，所以默认设置一个。
     * - dynamic_partition.enable: 用于指定表级别的动态分区功能是否开启。默认为 true。
     * - dynamic_partition.time_unit: 用于指定动态添加分区的时间单位，可选择为DAY（天），WEEK(周)，MONTH（月），HOUR（时）。
     * - dynamic_partition.end: 用于指定提前创建的分区数量。值必须大于0。
     * - dynamic_partition.prefix: 用于指定创建的分区名前缀，例如分区名前缀为p，则自动创建分区名为p20200108。
     */
    Map<String, String> DEFAULT_CREATE_PROPERTIES =
            Map.of(
                    "replication_allocation", "tag.location.default: 1");

    String DYNAMIC_PARTITION_ENABLED = "dynamic_partition.enable";
    String DYNAMIC_PARTITION_TIME_UNIT="dynamic_partition.time_unit";
    String DYNAMIC_PARTITION_END="dynamic_partition.end";
    String DYNAMIC_PARTITION_PREFIX = "dynamic_partition.prefix";

    String DROP_SLQ = "drop table %s";
}
