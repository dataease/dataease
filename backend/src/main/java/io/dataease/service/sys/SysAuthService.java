package io.dataease.service.sys;


import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.base.domain.SysAuth;
import io.dataease.base.domain.SysAuthDetail;
import io.dataease.base.mapper.SysAuthMapper;
import io.dataease.base.mapper.ext.ExtSysAuthDetailMapper;
import io.dataease.base.mapper.ext.ExtSysAuthMapper;
import io.dataease.base.mapper.ext.ExtVAuthModelMapper;
import io.dataease.commons.constants.SystemConstants;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.controller.request.BaseTreeRequest;
import io.dataease.controller.request.SysAuthRequest;
import io.dataease.dto.SysAuthDTO;
import io.dataease.dto.SysDeptDTO;
import io.dataease.dto.VAuthModelDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SysAuthService {


    @Resource
    private ExtSysAuthMapper extSysAuthMapper;

    @Resource
    private SysAuthMapper sysAuthMapper;

    @Resource
    private ExtSysAuthDetailMapper extSysAuthDetailMapper;

    @Resource
    private ExtVAuthModelMapper extVAuthModelMapper;

    private static List<String> PRI_MODEL_TYPE = Arrays.asList("link","dataset","chart","panel");


    /**
     * @Description: 查询可见授权数据的数据如果是管理员（IsAdmin = true）且modelType 为link dataset chart panel可以查询到所有的数据，
     * 如果是普通用户，只能查询到自己的数据；但是 node_type 为spine 时 节点也会返回
     **/
    public List<VAuthModelDTO> searchAuthModelTree(BaseTreeRequest request) {
        CurrentUserDto currentUserDto = AuthUtils.getUser();
        request.setCreateBy(null);
        if(PRI_MODEL_TYPE.contains(request.getModelType())&&(currentUserDto.getIsAdmin() == null || !currentUserDto.getIsAdmin())){
            request.setCreateBy(currentUserDto.getUsername());
        }
        return extVAuthModelMapper.searchTree(request);
    }



    /**
     * @Description: 查询授权明细map
     **/
    public Map<String, List<SysAuthDetail>> searchAuthDetails(SysAuthRequest request) {
        List<SysAuthDTO> authDTOList = extSysAuthMapper.searchAuth(request);
        return Optional.ofNullable(authDTOList).orElse(new ArrayList<>()).stream()
                .collect(Collectors.toMap(SysAuthDTO::getAuthSource, SysAuthDTO::getSysAuthDetails));
    }

    /**
     * @Description: 每个类型的授权都会在表中预制各个授权项的模板 存在auth_id 中；
     **/
    public List<SysAuthDetail> searchAuthDetailsModel(String authType) {
        return extSysAuthDetailMapper.searchAuthTypeModel(authType);
    }

    public void authChange(SysAuthRequest request) {
        SysAuthDetail sysAuthDetail = request.getAuthDetail();
        //TODO 获取需要授权的资源id(当前节点和所有权限的下级节点)
        List<String> authSources = getAuthModels(request.getAuthSource(), request.getAuthSourceType());

        //TODO 获取需要被授权的目标id(部门当前节点和所有权限的下级节点)
        List<String> authTargets =getAuthModels(request.getAuthTarget(), request.getAuthTargetType());

        if(CollectionUtils.isNotEmpty(authSources)&& CollectionUtils.isNotEmpty(authTargets)){
            List<String> authIdChange = new ArrayList<>();
            authTargets.stream().forEach(authTarget -> {
                authSources.forEach(authSource ->{
                    String authId = checkAuth(authSource, request.getAuthSourceType(),authTarget,request.getAuthTargetType());
                    authIdChange.add(authId);
                });
            });
            // 授权修改
            extSysAuthDetailMapper.authDetailsChange(sysAuthDetail.getPrivilegeValue(),sysAuthDetail.getPrivilegeType(),authIdChange);
//            if(sysAuthDetail.getPrivilegeValue()==SystemConstants.PRIVILEGE_VALUE_ON){
//                //当前为开启1 >>> 关闭0 需要将权限级别（PrivilegeType）大于当前级别的全新都修改为关闭 0
//                extSysAuthDetailMapper.authDetailsChange(SystemConstants.PRIVILEGE_VALUE_OFF,sysAuthDetail.getPrivilegeType(),authIdChange);
//            }else{
//                //当前为关闭0 >>> 开启1 需要将权限级别（PrivilegeType）小于当前级别的全新都修改为开启 1
//                extSysAuthDetailMapper.authDetailsChange(SystemConstants.PRIVILEGE_VALUE_ON,sysAuthDetail.getPrivilegeType(),authIdChange);
//            }
        }
    }

    private List<String> getAuthModels(String id, String type) {
        List<VAuthModelDTO> vAuthModelDTOS = searchAuthModelTree(new BaseTreeRequest(id,type, SystemConstants.WITH_EXTEND.CHILDREN));
        List<String> authSources = Optional.ofNullable(vAuthModelDTOS).orElse(new ArrayList<>()).stream().map(VAuthModelDTO::getId)
                .collect(Collectors.toList());
        return authSources;
    }

    /**
     * @Description: 查询当前target 是否有存在授权 不存在 增加权限 并复制权限模板
     **/
    private String checkAuth(String authSource,String authSourceType,String authTarget,String authTargetType){
        String authId = extSysAuthMapper.findAuthId(authSource,authSourceType,authTarget,authTargetType);
        if(StringUtils.isEmpty(authId)){
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
            extSysAuthDetailMapper.copyAuthModel(authSourceType,authId,AuthUtils.getUser().getUsername());
        }

        return authId;
    }

}
