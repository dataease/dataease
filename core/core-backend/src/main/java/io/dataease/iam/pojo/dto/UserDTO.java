package io.dataease.iam.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Description: UserDTO
 */
@Data
public class UserDTO {

    /**
     * 用户姓名 最长50字符
     */
    @NotBlank(message = "用户名字不能为空")
    @Size(min = 1,max = 100, message = "组织机构描述 最长100字符")
    private String userName;

    /**
     * 用户ID,与外部ID值一样
     */
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    /**
     * 用户账号
     */
    @NotBlank(message = "用户账号不能为空")
    private String account;

    /**
     * 证件类型，1：身份证 2：统一社会信用代码 3：港澳通行证 4：台湾通行证 5：外籍人士永久居留证
     */
    private String cardType;

    /**
     * 证件号
     */
    private String idCard;

    /**
     * 手机号, 手机号不唯一
     */
    private String phoneNumber;

    /**
     * 为账户所属组织单位
     */
    private String organizationUuid;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 扩展字段，attributes为系统定义扩展字段 JSON数组对象
     */
    private List<Map<String, Object>> extendField;

    /**
     * 账户状态，1已禁用，0正常
     */
    private Integer locked;
}
