package io.dataease.plugins.xpack.auth.dto.response;

import io.dataease.plugins.common.annotation.PluginResultMap;
import lombok.Data;

import java.io.Serializable;

@Data
@PluginResultMap
public class XpackVAuthModel implements Serializable {

    private String id;

    private String name;

    private String label;

    private String pid;

    private String nodeType;

    private String modelType;

    private String modelInnerType;

    private String authType;

    private String createBy;

    private static final long serialVersionUID = 1L;
}
