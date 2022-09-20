package io.dataease.service.panel.applog;

import com.google.gson.Gson;
import io.dataease.controller.sys.request.KeyGridRequest;
import io.dataease.dto.SysLogDTO;
import io.dataease.dto.appTemplateMarket.AppLogGridDTO;
import io.dataease.ext.ExtAppLogMapper;
import io.dataease.ext.query.GridExample;
import io.dataease.plugins.common.base.mapper.PanelAppTemplateLogMapper;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

@Service
public class AppLogService {

    private Gson gson = new Gson();
    @Resource
    private PanelAppTemplateLogMapper appLogMapper;
    @Resource
    private ExtAppLogMapper extAppLogMapper;


    public List<AppLogGridDTO> query(KeyGridRequest request) {
        GridExample gridExample = request.convertExample();
        gridExample.setExtendCondition(request.getKeyWord());
        AppLogQueryParam logQueryParam = gson.fromJson(gson.toJson(gridExample), AppLogQueryParam.class);
        List<AppLogGridDTO> voLogs = extAppLogMapper.query(logQueryParam);
        return voLogs;
    }

    public void saveLog(SysLogDTO sysLogDTO) {

    }


}
