package io.dataease.api.dataset.dto;

import lombok.Data;

/**
 * @Author Junjun
 */
@Data
public class SqlLogDTO {
    /**
     * ID
     */
    private String id;

    /**
     * 数据集SQL节点ID
     */
    private String tableId;

    /**
     * 开始时间
     */
    private Long startTime;

    /**
     * 结束时间
     */
    private Long endTime;

    /**
     * 耗时(毫秒)
     */
    private Long spend;

    /**
     * 详细信息
     */
    private String sql;

    /**
     * 状态
     */
    private String status;
}
