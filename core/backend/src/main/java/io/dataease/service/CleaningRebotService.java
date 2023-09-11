package io.dataease.service;

import io.dataease.ext.CleaningRebotMapper;
import io.dataease.service.message.SysMsgService;
import io.dataease.service.sys.log.LogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service
public class CleaningRebotService {

    @Value("${dataease.clean-nobody-link:false}")
    private Boolean cleanNobodyLink;

    @Resource
    private CleaningRebotMapper cleaningRebotMapper;

    @Resource
    private LogService logService;

    @Resource
    private SysMsgService sysMsgService;

    public void execute() {
        int floatDept = 0;
        do {
            floatDept = cleaningRebotMapper.delFloatingDept();
        } while (floatDept > 0);
        cleaningRebotMapper.updateUserDept();
        cleaningRebotMapper.delFloatingRoleMapping();
        cleaningRebotMapper.delFloatingPanelShare();
        cleaningRebotMapper.delFloatingTargetShare();
        cleaningRebotMapper.delFloatingPanelStore();
        cleaningRebotMapper.delFloatingTargetStore();
        cleaningRebotMapper.delFloatingPanelLink();
        cleaningRebotMapper.delFloatingPanelLinkMapping();
        if (cleanNobodyLink) {
            cleaningRebotMapper.delFloatingCreatorLink();
            cleaningRebotMapper.delFloatingCreatorLinkMapping();
        }
        logService.cleanDisusedLog();
        sysMsgService.cleanDisusedMsg();
    }
}
