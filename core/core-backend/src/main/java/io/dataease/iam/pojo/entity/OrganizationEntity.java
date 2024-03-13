package io.dataease.iam.pojo.entity;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * Description: OrganizationEntity
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("iam_organization")
public class OrganizationEntity {

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 部门名称
     */
    @TableField("organization")
    private String organization;

    /**
     * DataEase中组织ID
     */
    @TableField("per_org_id")
    private Long perOrgId;

    /**
     * 所属父级组织机构的uuid或外部ID
     */
    @TableField("parent_uuid")
    private String parentUuid;

    /**
     * 本组织机构的uuid或外部ID
     */
    @TableField("organization_uuid")
    private String organizationUuid;

    /**
     * 组织机构描述 最长200字符
     */
    @TableField("description")
    private String description;

    /**
     * 扩展字典，为系统定义扩展字段 JSON数组对象
     */
    @TableField("extend_field")
    private String extendFields;

    /**
     * 状态
     */
    @TableField("status")
    private String status;

    /**
     * 是否删除 1：删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * per 中父节点ID
     */
    @TableField(exist = false)
    private Long perPOrgId;

    /**
     * per 中组织对应的普通角色Id
     */
    @TableField(exist = false)
    private Long perRoleId;
}
