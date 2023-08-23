package io.dataease.plugins.xpack.theme.dto;

import java.io.Serializable;

import io.dataease.plugins.common.annotation.PluginResultMap;
import lombok.Data;

@Data
@PluginResultMap
public class ThemeItem implements Serializable{

    private String key;

    private String val;
    
}
