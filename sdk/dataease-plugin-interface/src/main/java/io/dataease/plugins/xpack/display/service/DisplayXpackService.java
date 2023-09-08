package io.dataease.plugins.xpack.display.service;




import io.dataease.plugins.common.service.PluginComponentService;
import io.dataease.plugins.xpack.display.dto.response.SysSettingDto;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;


public abstract class DisplayXpackService extends PluginComponentService{


    public abstract List<SysSettingDto> systemSettings();

    public abstract Boolean save(Map<String,List<SysSettingDto>> systemParameterMap, List<MultipartFile> bodyFiles);

    public abstract String readBlob(Long id);
}
