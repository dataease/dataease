package io.dataease.plugins.common.dto;

import io.dataease.plugins.common.annotation.PluginResultMap;
import lombok.Data;
import java.io.Serializable;

@Data
@PluginResultMap
public class PluginSimpleTreeNode implements Serializable {

    private Long id;

    private Long pid;
}
