package io.dataease.plugins.xpack.ukey.dto.request;

import io.dataease.plugins.common.annotation.PluginResultMap;
import lombok.Data;

import java.io.Serializable;
@PluginResultMap
@Data
public class XpackUkeyDto implements Serializable {

    private static final long serialVersionUID = 447309072990546277L;
    private Long id;

    private Long userId;

    private String accessKey;

    private String secretKey;

    private Long createTime;

    private String status;

}
