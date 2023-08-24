package io.dataease.plugins.xpack.loginlimit.service;

import io.dataease.plugins.common.service.PluginComponentService;
import io.dataease.plugins.xpack.display.dto.response.SysSettingDto;
import io.dataease.plugins.xpack.loginlimit.dto.response.LoginLimitInfo;

import java.util.List;

public abstract class LoginLimitXpackService extends PluginComponentService {

    public abstract LoginLimitInfo info();

    public abstract void save(List<SysSettingDto> settings);

    public abstract Boolean isOpen();
}
