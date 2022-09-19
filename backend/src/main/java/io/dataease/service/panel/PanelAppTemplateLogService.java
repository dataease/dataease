package io.dataease.service.panel;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import io.dataease.commons.constants.CommonConstants;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.controller.request.panel.PanelAppTemplateApplyRequest;
import io.dataease.controller.request.panel.PanelAppTemplateRequest;
import io.dataease.controller.request.panel.PanelGroupRequest;
import io.dataease.dto.dataset.DataSetTaskDTO;
import io.dataease.plugins.common.base.domain.*;
import io.dataease.plugins.common.base.mapper.PanelAppTemplateLogMapper;
import io.dataease.plugins.common.base.mapper.PanelAppTemplateMapper;
import io.dataease.plugins.common.constants.DatasetType;
import io.dataease.service.chart.ChartViewFieldService;
import io.dataease.service.chart.ChartViewService;
import io.dataease.service.dataset.DataSetTableFieldsService;
import io.dataease.service.dataset.DataSetTableService;
import io.dataease.service.datasource.DatasourceService;
import org.apache.commons.lang3.StringUtils;
import org.pentaho.di.core.util.UUIDUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: wangjiahao
 * Date: 2022/9/8
 * Description:
 */
@Service
public class PanelAppTemplateLogService {

    @Resource
    private PanelAppTemplateLogMapper logMapper;

    public void newAppApplyLog(PanelAppTemplateLog appTemplateLog){
        appTemplateLog.setId(UUIDUtil.getUUIDAsString());
        appTemplateLog.setApplyTime(System.currentTimeMillis());
        appTemplateLog.setApplyPersion(AuthUtils.getUser().getUsername());
        logMapper.insert(appTemplateLog);
    }
}
