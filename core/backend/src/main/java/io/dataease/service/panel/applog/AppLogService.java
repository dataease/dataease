package io.dataease.service.panel.applog;

import io.dataease.commons.utils.AuthUtils;
import io.dataease.controller.request.panel.AppLogGridRequest;
import io.dataease.dto.appTemplateMarket.AppLogGridDTO;
import io.dataease.ext.ExtAppLogMapper;
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


    public List<AppLogGridDTO> query(AppLogGridRequest request) {
        request.setUserId(AuthUtils.getUser().getUserId());
        return extAppLogMapper.query(request);
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
