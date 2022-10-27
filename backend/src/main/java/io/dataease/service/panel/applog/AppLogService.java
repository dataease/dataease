package io.dataease.service.panel.applog;

import com.google.gson.Gson;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.controller.sys.request.KeyGridRequest;
import io.dataease.dto.SysLogDTO;
import io.dataease.dto.appTemplateMarket.AppLogGridDTO;
import io.dataease.ext.ExtAppLogMapper;
import io.dataease.ext.query.GridExample;
import io.dataease.plugins.common.base.mapper.PanelAppTemplateLogMapper;
import io.dataease.service.dataset.DataSetGroupService;
import io.dataease.service.datasource.DatasourceService;
import io.dataease.service.panel.PanelGroupService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AppLogService {

    private Gson gson = new Gson();
    @Resource
    private PanelAppTemplateLogMapper appLogMapper;
    @Resource
    private ExtAppLogMapper extAppLogMapper;
    @Resource
    private PanelGroupService panelGroupService;
    @Resource
    private DataSetGroupService dataSetGroupService;
    @Resource
    private DatasourceService datasourceService;


    public List<AppLogGridDTO> query(KeyGridRequest request) {
        GridExample gridExample = request.convertExample();
        gridExample.setExtendCondition(request.getKeyWord());
        AppLogQueryParam logQueryParam = gson.fromJson(gson.toJson(gridExample), AppLogQueryParam.class);
        logQueryParam.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        List<AppLogGridDTO> voLogs = extAppLogMapper.query(logQueryParam);
        return voLogs;
    }

    public void saveLog(SysLogDTO sysLogDTO) {

    }


    @Transactional
    public void deleteLogAndResource(AppLogGridDTO request) throws Exception {
        if (request.getDeleteResource()) {
            if (StringUtils.isNotEmpty(request.getPanelId())) {
                panelGroupService.deleteCircle(request.getPanelId());
            }
            if (StringUtils.isNotEmpty(request.getDatasetGroupId())) {
                dataSetGroupService.delete(request.getDatasetGroupId());
            }
            if (StringUtils.isNotEmpty(request.getDatasourceId())) {
                datasourceService.deleteDatasource(request.getDatasourceId());
            }
        }
        appLogMapper.deleteByPrimaryKey(request.getId());
    }
}
