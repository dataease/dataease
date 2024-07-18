package io.dataease.api.visualization.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

@Data
public class AppCoreDatasourceTaskVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 数据源ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dsId;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 更新方式
     */
    private String updateType;

    /**
     * 开始时间
     */
    private Long startTime;

    /**
     * 执行频率：0 一次性 1 cron
     */
    private String syncRate;

    /**
     * cron表达式
     */
    private String cron;

    /**
     * 简单重复间隔
     */
    private Long simpleCronValue;

    /**
     * 简单重复类型：分、时、天
     */
    private String simpleCronType;

    /**
     * 结束限制 0 无限制 1 设定结束时间
     */
    private String endLimit;

    /**
     * 结束时间
     */
    private Long endTime;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 上次执行时间
     */
    private Long lastExecTime;

    /**
     * 上次执行结果
     */
    private String lastExecStatus;

    private String extraData;

    /**
     * 任务状态
     */
    private String taskStatus;
}
