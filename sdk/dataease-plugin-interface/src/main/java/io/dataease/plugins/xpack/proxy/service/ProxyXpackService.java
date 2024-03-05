package io.dataease.plugins.xpack.proxy.service;

import io.dataease.plugins.common.service.PluginComponentService;
import io.dataease.plugins.xpack.display.dto.response.SysSettingDto;
import io.dataease.plugins.xpack.proxy.dto.ProxyInfo;

import java.util.List;

public abstract class ProxyXpackService extends PluginComponentService {

    public abstract ProxyInfo info();

    public abstract void save(List<SysSettingDto> settings);

    public abstract void setProxy();
}
