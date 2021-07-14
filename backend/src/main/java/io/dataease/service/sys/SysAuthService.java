package io.dataease.service.sys;


import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.base.domain.SysAuth;
import io.dataease.base.domain.SysAuthDetail;
import io.dataease.base.mapper.SysAuthMapper;
import io.dataease.base.mapper.ext.ExtSysAuthDetailMapper;
import io.dataease.base.mapper.ext.ExtSysAuthMapper;
import io.dataease.commons.constants.SystemConstants;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.controller.request.BaseTreeRequest;
import io.dataease.controller.request.SysAuthRequest;
import io.dataease.dto.SysAuthDetailDTO;
import io.dataease.i18n.Translator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
public class SysAuthService {


    @Resource
    private ExtSysAuthMapper extSysAuthMapper;

    @Resource
    private SysAuthMapper sysAuthMapper;

    @Resource
    private ExtSysAuthDetailMapper extSysAuthDetailMapper;


    private static List<String> PRI_MODEL_TYPE = Arrays.asList("link", "dataset", "chart", "panel", "menu");



    /**
     * @Description: 查询授权明细map
     **/
    public Map<String, List<SysAuthDetailDTO>> searchAuthDetails(SysAuthRequest request) {
        List<SysAuthDetailDTO> authDetailDTOList = extSysAuthMapper.searchAuth(request);
        return Optional.ofNullable(authDetailDTOList).orElse(new ArrayList<>()).stream()
                .collect(groupingBy(SysAuthDetailDTO::getAuthSource));
    }

    /**
     * @Description: 每个类型的授权都会在表中预制各个授权项的模板 存在auth_id 中；
     **/
    public List<SysAuthDetail> searchAuthDetailsModel(String authType) {
        return extSysAuthDetailMapper.searchAuthTypeModel(authType);
    }

    /**
     * @Description: 查询当前target 是否有存在授权 不存在 增加权限 并复制权限模板
     **/
    private String checkAuth(String authSource, String authSourceType, String authTarget, String authTargetType) {
        String authId = extSysAuthMapper.findAuthId(authSource, authSourceType, authTarget, authTargetType);
        if (StringUtils.isEmpty(authId)) {
            authId = UUID.randomUUID().toString();
            //TODO 插入权限
            SysAuth sysAuthRecord = new SysAuth();
            sysAuthRecord.setId(authId);
            sysAuthRecord.setAuthSource(authSource);
            sysAuthRecord.setAuthSourceType(authSourceType);
            sysAuthRecord.setAuthTarget(authTarget);
            sysAuthRecord.setAuthTargetType(authTargetType);
            sysAuthRecord.setAuthTime(System.currentTimeMillis());
            sysAuthRecord.setAuthUser(AuthUtils.getUser().getUsername());
            sysAuthMapper.insertSelective(sysAuthRecord);

            //TODO 复制权限模板
            extSysAuthDetailMapper.copyAuthModel(authSourceType, authId, AuthUtils.getUser().getUsername());
        }

        return authId;
    }

    public void checkTreeNoManageCount(String modelType,String nodeId){
        if(extSysAuthMapper.checkTreeNoManageCount(AuthUtils.getUser().getUserId(),modelType,nodeId)){
            throw new RuntimeException(Translator.get("i18n_no_all_delete_privilege_folder"));
        }
    }

}
