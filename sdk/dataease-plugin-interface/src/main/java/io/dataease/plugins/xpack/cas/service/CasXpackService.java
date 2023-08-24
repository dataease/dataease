package io.dataease.plugins.xpack.cas.service;

import io.dataease.plugins.common.service.PluginComponentService;
import io.dataease.plugins.xpack.cas.dto.CasSaveResult;
import io.dataease.plugins.xpack.display.dto.response.SysSettingDto;

import javax.servlet.ServletContext;
import java.util.List;

public abstract class CasXpackService extends PluginComponentService {

    public abstract List<SysSettingDto> casSettings();

    public abstract CasSaveResult save(List<SysSettingDto> parameters);

    public Boolean supportCas() {
        return false;
    }

    public abstract void checkCasStatus(ServletContext servletContext);

    public abstract void setEnabled(Boolean valid);

    public abstract String logout();


}
