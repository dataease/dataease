package io.dataease.service.panel;

import io.dataease.commons.utils.AuthUtils;
import io.dataease.plugins.common.base.domain.*;
import io.dataease.plugins.common.base.mapper.PanelAppTemplateLogMapper;
import org.pentaho.di.core.util.UUIDUtil;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

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
