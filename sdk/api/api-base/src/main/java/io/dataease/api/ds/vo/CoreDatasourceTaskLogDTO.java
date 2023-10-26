package io.dataease.api.ds.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author fit2cloud
 * @since 2023-06-08
 */
@Data
public class CoreDatasourceTaskLogDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    /**
     * 数据源ID
     */
    @JsonSerialize(using= ToStringSerializer.class)
    private Long dsId;

    /**
     * 任务ID
     */
    @JsonSerialize(using= ToStringSerializer.class)
    private Long taskId;

    /**
     * 开始时间
     */
    private Long startTime;

    /**
     * 结束时间
     */
    private Long endTime;

    /**
     * 执行状态
     */
    private String taskStatus;
    private String tableName;
    private String name;

    /**
     * 错误信息
     */
    private String info;

    /**
     * 创建时间
     */
    private Long createTime;

    private String triggerType;

}
