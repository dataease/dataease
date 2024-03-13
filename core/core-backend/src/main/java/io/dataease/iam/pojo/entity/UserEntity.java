package io.dataease.iam.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * Description: UserEntity
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("iam_user")
public class UserEntity {

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户姓名 最长50字符
     */
    @TableField(value = "user_name")
    private String userName;

    /**
     * 用户ID,与外部ID值一样
     */
    @TableField(value = "user_id")
    private String userId;

    /**
     * DataEase用户ID
     */
    @TableField(value = "per_user_id")
    private Long perUserId;

    /**
     * 用户账号
     */
    private String account;

    /**
     * 证件类型，1：身份证 2：统一社会信用代码 3：港澳通行证 4：台湾通行证 5：外籍人士永久居留证
     */
    @TableField(value = "card_type")
    private String cardType;

    /**
     * 证件号
     */
    @TableField(value = "id_card")
    private String idCard;

    /**
     * 手机号, 手机号不唯一
     */
    @TableField(value = "phone_number")
    private String phoneNumber;

    /**
     * 为账户所属组织单位
     */
    @TableField(value = "organization_uuid")
    private String organizationUuid;

    /**
     * 扩展字段，attributes为系统定义扩展字段 JSON数组对象
     */
    @TableField(value = "extend_field")
    private String extendField;

    /**
     * web,app，用户信息区分，1web，2app
     */
    private String type;

    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 主题颜色
     */
    private String color;

    /**
     * 主题样式
     */
    private String layout;

    /**
     * per_org 中 所属组织ID
     */
    @TableField(exist = false)
    private Long perOrgId;

    /**
     * per_role 中 所属组织的角色ID
     */
    @TableField(exist = false)
    private Long perRoleId;

    /**
     * 账户状态，1已禁用，0正常
     */
    @TableField(exist = false)
    private Integer locked;
}
