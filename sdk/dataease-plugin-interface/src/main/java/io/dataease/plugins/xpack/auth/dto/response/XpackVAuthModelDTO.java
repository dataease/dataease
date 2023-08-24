package io.dataease.plugins.xpack.auth.dto.response;

import io.dataease.plugins.common.annotation.PluginResultMap;
import lombok.Data;

import java.util.List;

@Data
@PluginResultMap
public class XpackVAuthModelDTO extends XpackVAuthModel{

    private List<XpackVAuthModelDTO> children;

    private Boolean leaf;

    private Integer childrenCount;

    private Boolean hasChildren;
}
