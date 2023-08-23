package io.dataease.plugins.xpack.theme.dto;

import java.io.Serializable;

import io.dataease.plugins.common.annotation.PluginResultMap;
import lombok.Data;

@Data
@PluginResultMap
public class ThemeDto implements Serializable{

    private Integer id;

    private String name;

    private Boolean senior;

    private Boolean status;

    private Integer originId;
    
}
