package io.dataease.plugins.xpack.auth.dto.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class DatasetColumnPermissions implements Serializable {
    private String id;

    private String authTargetType;

    private Long authTargetId;

    private String datasetId;

    private Long updateTime;

    private String permissions;

    @io.swagger.annotations.ApiModelProperty("白名单-用户ID->JSON Array")
    private java.lang.String whiteListUser;

    private static final long serialVersionUID = 1L;
}