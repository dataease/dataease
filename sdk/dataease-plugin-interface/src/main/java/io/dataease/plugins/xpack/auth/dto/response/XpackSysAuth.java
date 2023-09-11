package io.dataease.plugins.xpack.auth.dto.response;

import io.dataease.plugins.common.annotation.PluginResultMap;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@PluginResultMap
public class XpackSysAuth implements Serializable {

    private String id;

    private String authSource;

    private String authSourceType;

    private String authTarget;

    private String authTargetType;

    private Long authTime;

    private String authDetails;

    private String authUser;

    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
