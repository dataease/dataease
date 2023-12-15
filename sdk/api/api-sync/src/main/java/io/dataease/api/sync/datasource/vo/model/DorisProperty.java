package io.dataease.api.sync.datasource.vo.model;

import lombok.Data;

/**
 * @author fit2cloud
 * @date 2023/9/8 14:56
 **/
@Data
public class DorisProperty {
    /**
     * 启用分区on
     */
    private String partitionEnable;

    /**
     * 分区类型
     * DateRange 日期
     * NumberRange 数值
     * List 列
     */
    private String partitionType;
    /**
     * 启用动态分区on
     */
    private String dynamicPartitionEnable;
    /**
     * 动态分区结束偏移量
     */
    private int dynamicPartitionEnd;
    /**
     * 动态分区间隔单位
     * HOUR WEEK DAY MONTH YEAR
     */
    private String dynamicPartitionTimeUnit;
    /**
     * 手动分区列值
     * 多个以','隔开
     */
    private String manualPartitionColumnValue;
    /**
     * 手动分区数值区间开始值
     */
    private int manualPartitionStart;
    /**
     * 手动分区数值区间结束值
     */
    private int manualPartitionEnd;
    /**
     * 手动分区数值区间间隔
     */
    private int manualPartitionInterval;
    /**
     * 手动分区日期区间
     * 2023-09-08 - 2023-09-15
     */
    private String manualPartitionTimeRange;
    /**
     * 手动分区日期区间间隔单位
     */
    private String manualPartitionTimeUnit;
    /**
     * 分区字段
     */
    private String partitionColumn;
}
