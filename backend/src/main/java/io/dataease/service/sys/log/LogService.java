package io.dataease.service.sys.log;


import com.google.gson.Gson;
import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.commons.constants.SysLogConstants;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.controller.sys.base.ConditionEntity;
import io.dataease.dto.SysLogDTO;
import io.dataease.dto.SysLogGridDTO;
import io.dataease.dto.log.FolderItem;
import io.dataease.ext.ExtSysLogMapper;
import io.dataease.ext.query.GridExample;
import io.dataease.i18n.Translator;
import io.dataease.plugins.common.base.domain.SysLogWithBLOBs;
import io.dataease.plugins.common.base.mapper.SysLogMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LogService {

    private Gson gson = new Gson();

    private static Integer[] unPanelOperates = {4, 5, 8, 9, 10};

    @Resource
    private SysLogMapper sysLogMapper;

    @Resource
    private ExtSysLogMapper extSysLogMapper;

    @Resource
    private LogManager logManager;



    public List<SysLogGridDTO> query(BaseGridRequest request) {
        List<ConditionEntity> conditions = request.getConditions();
        if (CollectionUtils.isNotEmpty(conditions)) {
            ConditionEntity optypeCondition = null;
            ConditionEntity sourceCondition = null;
            int matchIndex = -1;
            for (int i = 0; i < conditions.size(); i++) {
                ConditionEntity conditionEntity = conditions.get(i);
                String field = conditionEntity.getField();
                Object value = conditionEntity.getValue();

                if (StringUtils.isNotBlank(field) && StringUtils.equals("optype", field) && ObjectUtils.isNotEmpty(value)) {
                    matchIndex = i;
                    optypeCondition = new ConditionEntity();
                    sourceCondition = new ConditionEntity();
                    List<String> values = (List<String>) value;
                    sourceCondition.setField("source_type");
                    optypeCondition.setField("operate_type");
                    List<Integer> opValue = values.stream().map(v -> Integer.parseInt(v.split("-")[0])).collect(Collectors.toList());
                    List<Integer> soValue = values.stream().map(v -> Integer.parseInt(v.split("-")[1])).collect(Collectors.toList());
                    optypeCondition.setValue(opValue);
                    sourceCondition.setValue(soValue);
                    optypeCondition.setOperator(conditionEntity.getOperator());
                    sourceCondition.setOperator(conditionEntity.getOperator());
                }
            }
            if (matchIndex >= 0 ) {
                conditions.remove(matchIndex);
                if (ObjectUtils.isNotEmpty(optypeCondition))conditions.add(optypeCondition);
                if (ObjectUtils.isNotEmpty(sourceCondition))conditions.add(sourceCondition);
            }
        }

        GridExample gridExample = request.convertExample();
        List<SysLogWithBLOBs> voLogs = extSysLogMapper.query(gridExample);
        List<SysLogGridDTO> dtos = voLogs.stream().map(this::convertDTO).collect(Collectors.toList());
        return dtos;
    }


    public List<FolderItem> types() {
        List<Integer> integers = Arrays.stream(unPanelOperates).collect(Collectors.toList());
        List<FolderItem> results = new ArrayList<>();
        SysLogConstants.SOURCE_TYPE[] sourceTypes = SysLogConstants.SOURCE_TYPE.values();
        SysLogConstants.OPERATE_TYPE[] operateTypes = SysLogConstants.OPERATE_TYPE.values();
        for (int i = 0; i < sourceTypes.length; i++) {
            SysLogConstants.SOURCE_TYPE sourceType = sourceTypes[i];
            for (int j = 0; j < operateTypes.length; j++) {
                SysLogConstants.OPERATE_TYPE operateType = operateTypes[j];
                if (sourceType.getValue() > 3 && operateType.getValue() > 3){
                    continue;
                }
                if (sourceType.getValue() != 3 && integers.contains(operateType.getValue())) {
                    continue;
                }
                FolderItem folderItem = new FolderItem();
                folderItem.setId(operateType.getValue() + "-" + sourceType.getValue());
                String operateTypeName = operateType.getName();
                String sourceTypeName = sourceType.getName();
                folderItem.setName( Translator.get(operateTypeName) + Translator.get(sourceTypeName));
                results.add(folderItem);
            }
        }

        return results;
    }

    public SysLogGridDTO convertDTO(SysLogWithBLOBs vo) {
        SysLogGridDTO sysLogGridDTO = new SysLogGridDTO();
        sysLogGridDTO.setOpType(SysLogConstants.operateTypeName(vo.getOperateType()));
        sysLogGridDTO.setSourceType(SysLogConstants.sourceTypeName(vo.getSourceType()));
        sysLogGridDTO.setTime(vo.getTime());
        sysLogGridDTO.setUser(vo.getNickName());
        sysLogGridDTO.setDetail(logManager.detailInfo(vo));
        return sysLogGridDTO;
    }

    public void saveLog(SysLogDTO sysLogDTO) {
        // String ip = "";
        CurrentUserDto user = AuthUtils.getUser();
        SysLogWithBLOBs sysLogWithBLOBs = BeanUtils.copyBean(new SysLogWithBLOBs(), sysLogDTO);
        if (CollectionUtils.isNotEmpty(sysLogDTO.getPositions())) {
            sysLogWithBLOBs.setPosition(gson.toJson(sysLogDTO.getPositions()));
        }
        if (CollectionUtils.isNotEmpty(sysLogDTO.getRemarks())) {
            sysLogWithBLOBs.setRemark(gson.toJson(sysLogDTO.getRemarks()));
        }
        sysLogWithBLOBs.setTime(System.currentTimeMillis());
        sysLogWithBLOBs.setUserId(user.getUserId());
        sysLogWithBLOBs.setLoginName(user.getUsername());
        sysLogWithBLOBs.setNickName(user.getNickName());
        // sysLogWithBLOBs.setIp(ip);
        sysLogMapper.insert(sysLogWithBLOBs);
    }

    public void exportExcel(HttpServletResponse response) throws Exception{

    }

}
