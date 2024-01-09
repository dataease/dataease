package io.dataease.api.dataset.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "数据集结构")
@Data
public class DatasetNodeDTO implements Serializable {

    /**
     * ID
     */
    @Schema(description = "ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 名称
     */
    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    /**
     * 父级ID
     */
    @Schema(description = "父级目录ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pid;

    /**
     * 当前分组处于第几级
     */
    @Schema(description = "层级")
    private Integer level;

    /**
     * node类型：folder or dataset
     */
    @Schema(description = "叶子节点类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nodeType;

    /**
     * sql,union
     */
    @Schema(description = "数据集类型")
    private String type;

    /**
     * 连接模式：0-直连，1-同步(包括excel、api等数据存在de中的表)
     */
    @Schema(description = "连接模式", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer mode;

    /**
     * 关联关系树
     */
    @Schema(description = "关联tree", requiredMode = Schema.RequiredMode.REQUIRED)
    private String info;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID")
    private String createBy;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Long createTime;

    private String qrtzInstance;

    /**
     * 同步状态
     */
    private String syncStatus;

    /**
     * 创建人ID
     */
    private String updateBy;

    /**
     * 最后同步时间
     */
    private Long lastUpdateTime;

    /**
     * 关联sql,由tree
     */
    private String unionSql;

}
