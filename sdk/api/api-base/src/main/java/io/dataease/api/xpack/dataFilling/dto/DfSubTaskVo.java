package io.dataease.api.xpack.dataFilling.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class DfSubTaskVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 7440756474905083085L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long taskId;

    private Long startTime;

    private Long endTime;

    /**
     * 任务下发状态
     */
    private Integer execStatus;

    /**
     * 任务状态
     * 1：进行中/ 0：已过期 根据endTime判断
     */
    private Integer status;

    /**
     * 所有任务项 subInstance 个数
     */
    private int totalCount;
    private int unfinishedCount;

    /**
     * 完成率
     * (totalCount - unfinishedCount) / totalCount
     */
    private BigDecimal finishedRate;

    private int totalUserCount;
    private int unfinishedUserCount;


}
