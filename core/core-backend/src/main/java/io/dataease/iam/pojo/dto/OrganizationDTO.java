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
public class OrganizationDTO {

    /**
     * 部门名称
     */
    @Size(min = 1, max = 100, message = "组织机构描述 最长100字符")
    private String organization;

    /**
     * 所属父级组织机构的uuid或外部ID
     */
    @NotBlank(message = "所属父级组织机构的uuid或外部ID不能为空")
    private String parentUuid;

    /**
     * 本组织机构的uuid或外部ID
     */
    @NotBlank(message = "本组织机构的uuid或外部ID不能为空")
    private String organizationUuid;

    /**
     * 组织机构描述 最长200字符
     */
    private String description;

    /**
     * 扩展字典，为系统定义扩展字段 JSON数组对象
     */
    private List<Map<String, Object>> extendField;

    /**
     * 状态
     */
    private String status;
}
