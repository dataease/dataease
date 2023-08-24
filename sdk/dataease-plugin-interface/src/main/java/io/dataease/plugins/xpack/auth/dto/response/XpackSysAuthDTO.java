package io.dataease.plugins.xpack.auth.dto.response;

import io.dataease.plugins.common.annotation.PluginResultMap;
import lombok.Data;

import java.util.List;

@Data
@PluginResultMap
public class XpackSysAuthDTO extends XpackSysAuth{

    private List<XpackSysAuthDetail> sysAuthDetails;

}
