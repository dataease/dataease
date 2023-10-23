package io.dataease.api.permissions.dataset.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.dataease.api.permissions.user.vo.UserFormVO;
import lombok.Data;

import java.util.List;

@Data
public class DataSetColumnPermissionsDTO   {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 是否启用
     */
    private Boolean enable;

    /**
     * 权限类型：组织/角色/用户
     */
    private String authTargetType;

    /**
     * 权限对象ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long authTargetId;

    /**
     * 数据集ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long datasetId;

    /**
     * 权限
     */
    private String permissions;

    /**
     * 白名单
     */
    private String whiteListUser;

    private Long updateTime;
    private String datasetName;
    private String authTargetName;
    private List<Long> authTargetIds;

    private List<UserFormVO> whiteListUsers;
}
