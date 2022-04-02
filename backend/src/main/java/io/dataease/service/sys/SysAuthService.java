package io.dataease.service.sys;


import io.dataease.base.mapper.ext.ExtSysAuthMapper;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.i18n.Translator;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SysAuthService {

    @Resource
    private ExtSysAuthMapper extSysAuthMapper;

    public void checkTreeNoManageCount(String modelType, String nodeId) {
        if (extSysAuthMapper.checkTreeNoManageCount(AuthUtils.getUser().getUserId(), modelType, nodeId)) {
            throw new RuntimeException(Translator.get("i18n_no_all_delete_privilege_folder"));
        }
    }

    public void copyAuth(String authSource,String authSourceType){
        String userName = AuthUtils.getUser().getUsername();
        extSysAuthMapper.copyAuth(authSource,authSourceType,userName);
    }

}
