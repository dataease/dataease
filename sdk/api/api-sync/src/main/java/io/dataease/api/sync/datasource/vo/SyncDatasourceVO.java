package io.dataease.api.sync.datasource.vo;

import lombok.Data;

/**
 * @author fit2cloud
 * @date 2023/11/20 11:18
 **/
@Data
public class SyncDatasourceVO {
    /**
     * ID
     */
    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String desc;

    /**
     * 类型
     */
    private String type;

    /**
     * 详细信息
     */
    private String configuration;

    /**
     * Create timestamp
     */
    private Long createTime;

    /**
     * Update timestamp
     */
    private Long updateTime;

    /**
     * 创建人
     */
    private Long createBy;
    private String createByName;

    /**
     * 状态
     */
    private String status;
    private String statusRemark;
}
