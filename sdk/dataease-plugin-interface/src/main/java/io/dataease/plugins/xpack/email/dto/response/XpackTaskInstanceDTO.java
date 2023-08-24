package io.dataease.plugins.xpack.email.dto.response;

import java.io.Serializable;

import io.dataease.plugins.common.annotation.PluginResultMap;
import lombok.Data;

@PluginResultMap
@Data
public class XpackTaskInstanceDTO implements Serializable{

    private Long taskId;

    private Long instanceId;

    private String taskName;

    private Long executeTime;

    private Integer status;

    private String info;
    
}
