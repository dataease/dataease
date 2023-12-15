package io.dataease.api.sync.datasource.vo.catalog.doris;

import io.dataease.api.sync.datasource.vo.model.TableField;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * doris 表参数
 * @author fit2cloud
 * @date 2023/8/31 18:41
 **/
@Data
@Builder
public class DorisConfig {

    /**
     * 建表脚本模版
     */
    private String tableTemplate;
    /**
     * 分桶数量，默认10
     */
    private String distributionBucket;
    /**
     * 表参数
     */
    private Map<String, String> properties;
    /**
     * 启用分区
     */
    private boolean partitionEnable;
    /**
     * 启用动态分区
     */
    private boolean dynamicEnable;
    /**
     * 分区调度单位HOUR、DAY、WEEK、MONTH、YEAR
     */
    private PartitionTimeUnitEnum dynamicPartitionTimeUnitEnum;
    /**
     * 动态分区的结束偏移,提前创建对应范围的分区
     */
    private int dynamicPartitionEnd;
    /**
     * 分区类型 Range List
     */
    private String partitionType;
    /**
     * 分区字段
     */
    private String partitionColumn;
    /**
     * 分区调度单位HOUR、DAY、WEEK、MONTH、YEAR
     */
    private PartitionTimeUnitEnum partitionTimeUnitEnum;
    /**
     * 手动分区生效
     * 当分区方式为Range时
     * 时间类型字段：key:start;value:yyyy-MM-dd HH:mm:ss,key:end;value:yyyy-MM-dd HH:mm:ss
     * 数值类型字段：key:start:value:1,key:end:3
     * 当分区方式为List时，key为分区值名称，value为字符串集合
     */
    private Map<String, String> partitionRange;

    /**
     * 手动分区生效
     * 数值分区间隔
     */
    private int partitionInterval;

    /**
     * 表字段
     */
    private List<TableField> columns;
    /**
     * 主键
     */
    private List<String> primaryKeys;
    /**
     * 索引列
     */
    private List<String> indexFields;

    /**
     * 表描述
     */
    private String comment;

    /**
     * 源数据库类型
     */
    private String sourceDbType;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 数据库名
     */
    private String database;
}
