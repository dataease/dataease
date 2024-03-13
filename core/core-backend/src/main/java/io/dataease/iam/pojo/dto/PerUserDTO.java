package io.dataease.iam.pojo.dto;

import lombok.Data;

import java.util.List;

/**
 * per_user参数
 */
@Data
public class PerUserDTO {

    private String name;

    private String account;

    private String email;

    /**
     * 需要根据组织id，查询pre_role
     */
    private List<Long> roleIds;

    private boolean enable;
}
