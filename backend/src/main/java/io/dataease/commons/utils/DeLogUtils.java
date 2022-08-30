package io.dataease.commons.utils;

import io.dataease.commons.constants.SysLogConstants;
import io.dataease.dto.SysLogDTO;
import io.dataease.dto.log.FolderItem;
import io.dataease.service.sys.log.LogManager;
import io.dataease.service.sys.log.LogService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.dataease.commons.constants.SysLogConstants.OPERATE_TYPE;
import static io.dataease.commons.constants.SysLogConstants.SOURCE_TYPE;

@Component
public class DeLogUtils {


    private static LogManager logManager;

    private static LogService logService;

    @Autowired
    public void setLogManager(LogManager logManager) {
        DeLogUtils.logManager = logManager;
    }

    @Autowired
    public void setLogService(LogService logService) {
        DeLogUtils.logService = logService;
    }

    public static SysLogDTO buildLog(OPERATE_TYPE operatetype, SOURCE_TYPE sourcetype, Object sourceIdValue, Object targetId, SOURCE_TYPE target_type ) {
        SysLogDTO sysLogDTO = buildLog(operatetype, sourcetype, sourceIdValue, null, targetId, target_type);
        if (sourcetype == SysLogConstants.SOURCE_TYPE.DATASOURCE) {
            FolderItem folderItem = logManager.dsTypeInfoById(sourceIdValue.toString());
            List<FolderItem> items = new ArrayList<>();
            items.add(folderItem);
            sysLogDTO.setPositions(items);
        }else {
            List<FolderItem> parentsAndSelf = logManager.justParents(sourceIdValue.toString(), sourcetype);
            sysLogDTO.setPositions(parentsAndSelf);
        }
        return sysLogDTO;
    }


    public static SysLogDTO buildBindRoleUserLog(Long positionId, Long userId, OPERATE_TYPE operatetype, SOURCE_TYPE positionType) {
        SysLogDTO sysLogDTO = new SysLogDTO();
        sysLogDTO.setOperateType(operatetype.getValue());
        sysLogDTO.setSourceType(SOURCE_TYPE.USER.getValue());
        sysLogDTO.setSourceId(userId.toString());
        FolderItem sourceInfo = logManager.nameWithId(userId.toString(), SOURCE_TYPE.USER.getValue());
        if (ObjectUtils.isEmpty(sourceInfo)) {
            return null;
        }
        sysLogDTO.setSourceName(sourceInfo.getName());

        List<FolderItem> parentsAndSelf = logManager.parentsAndSelf(positionId.toString(), positionType);
        sysLogDTO.setPositions(parentsAndSelf);

        return sysLogDTO;
    }

    public static SysLogDTO buildLog(OPERATE_TYPE operatetype, SOURCE_TYPE sourcetype, Object sourceIdValue, Object positionId, Object targetId, SOURCE_TYPE target_type ) {
        SysLogDTO sysLogDTO = new SysLogDTO();
        sysLogDTO.setOperateType(operatetype.getValue());
        sysLogDTO.setSourceType(sourcetype.getValue());
        sysLogDTO.setSourceId(sourceIdValue.toString());
        FolderItem sourceInfo = logManager.nameWithId(sourceIdValue.toString(), sourcetype.getValue());
        if (ObjectUtils.isEmpty(sourceInfo)) {
            return null;
        }
        sysLogDTO.setSourceName(sourceInfo.getName());

        if (ObjectUtils.isNotEmpty(positionId)) {
            if (sourcetype == SysLogConstants.SOURCE_TYPE.DATASOURCE) {
                FolderItem folderItem = logManager.dsTypeInfo(positionId.toString());
                List<FolderItem> items = new ArrayList<>();
                items.add(folderItem);
                sysLogDTO.setPositions(items);
            }else {
                if(sourcetype == SOURCE_TYPE.DRIVER_FILE){
                    List<FolderItem> parentsAndSelf = logManager.parentsAndSelf(positionId.toString(), SOURCE_TYPE.DRIVER);
                    sysLogDTO.setPositions(parentsAndSelf);
                }else if(sourcetype == SOURCE_TYPE.VIEW){
                    List<FolderItem> parentsAndSelf = logManager.parentsAndSelf(positionId.toString(), SOURCE_TYPE.PANEL);
                    sysLogDTO.setPositions(parentsAndSelf);
                }else {
                    List<FolderItem> parentsAndSelf = logManager.parentsAndSelf(positionId.toString(), sourcetype);
                    sysLogDTO.setPositions(parentsAndSelf);
                }

            }
        }

        if (ObjectUtils.isNotEmpty(targetId)) {
            List<FolderItem> parentsAndSelf = logManager.parentsAndSelf(targetId.toString(), target_type);
            sysLogDTO.setRemarks(parentsAndSelf);
        }
        return sysLogDTO;
    }

    public static void save(SysLogDTO sysLogDTO) {
        Optional.ofNullable(sysLogDTO).ifPresent(logService::saveLog);
    }

    public static void save(OPERATE_TYPE operatetype, SOURCE_TYPE sourcetype, Object sourceIdValue, Object positionId, Object targetId, SOURCE_TYPE target_type) {
        SysLogDTO sysLogDTO = buildLog(operatetype, sourcetype, sourceIdValue, positionId, targetId, target_type);
        Optional.ofNullable(sysLogDTO).ifPresent(logService::saveLog);
    }


}
