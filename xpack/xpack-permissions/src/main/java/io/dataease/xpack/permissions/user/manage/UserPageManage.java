package io.dataease.xpack.permissions.user.manage;


import io.dataease.api.permissions.role.dto.UserRequest;
import io.dataease.api.permissions.user.dto.UserCreator;
import io.dataease.api.permissions.user.dto.UserEditor;
import io.dataease.api.permissions.user.vo.CurUserVO;
import io.dataease.api.permissions.user.vo.UserFormVO;
import io.dataease.api.permissions.user.vo.UserItem;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;

import io.dataease.utils.Md5Utils;
import io.dataease.xpack.permissions.user.dao.auto.entity.PerUser;
import io.dataease.xpack.permissions.user.dao.auto.mapper.PerUserMapper;
import io.dataease.xpack.permissions.user.dao.ext.mapper.UserExtMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Lazy
public class UserPageManage {

    @Resource
    private PerUserMapper perUserMapper;

    @Resource
    private UserExtMapper userExtMapper;

    @Resource
    private RoleManage roleManage;



    private static final String DEFAULT_PWD = "DataEase123456";

    public void save(UserCreator creator) {
        TokenUserBO user = AuthUtils.getUser();
        PerUser perUser = BeanUtils.copyBean(new PerUser(), creator);
        perUser.setCreateTime(System.currentTimeMillis());
        perUser.setId(IDUtils.snowID());
        perUser.setDefaultOid(user.getDefaultOid());
        perUser.setCreatorId(user.getUserId());
        perUser.setOrigin(0);
        perUser.setLanguage("zh-CN");
        perUser.setPwd(Md5Utils.md5(DEFAULT_PWD));
        perUserMapper.insert(perUser);
        roleManage.mountWithUserSave(perUser.getId(), creator.getRoleIds());
    }

    public void edit(UserEditor editor) {
        PerUser perUser = BeanUtils.copyBean(new PerUser(), editor);
        perUserMapper.updateById(perUser);
        roleManage.mountWithUserSave(perUser.getId(), editor.getRoleIds());
    }

    public void delete(Long id) {
        int rCount = userExtMapper.userRoleMappingCount(id);
        if (rCount > 0) {
            Long oid = AuthUtils.getUser().getDefaultOid();
            userExtMapper.deleteUserRoleMapping(id, oid);
            rCount = userExtMapper.userRoleMappingCount(id);
        }
        if (rCount == 0) {
            perUserMapper.deleteById(id);
        }
    }

    public UserFormVO queryForm(Long uid) {
        UserFormVO userFormVO = userExtMapper.queryForm(uid, AuthUtils.getUser().getDefaultOid());
        return userFormVO;
    }

    public List<UserItem> optionForRole(UserRequest request) {
        Long defaultOid = AuthUtils.getUser().getDefaultOid();
        return userExtMapper.optionForRole(request.getRid(), defaultOid, request.getKeyword());
    }

    public List<UserItem> selectedForRole(UserRequest request) {
        return userExtMapper.selectedForRole(request.getRid(), request.getKeyword());
    }

    public void switchOrg(Long oId) {
        TokenUserBO user = AuthUtils.getUser();
        // List<PerOrgItem> perOrgItems = orgPageManage.queryByUser(user.getUserId(), null);
        /*if (CollectionUtil.isNotEmpty(perOrgItems) && perOrgItems.stream().filter(item -> !item.isDisabled()).anyMatch(item -> item.getId() == oId)) {
            userExtMapper.switchOrg(user.getUserId(), oId);
            return;
        }*/
        userExtMapper.switchOrg(user.getUserId(), oId);
        // DEException.throwException("invalid orgid");
    }

    public CurUserVO getUserInfo() {
        Long userId = AuthUtils.getUser().getUserId();
        return userExtMapper.userInfo(userId);
    }

    public void deleteByEmptyRole() {
       userExtMapper.deleteByEmptyRole();
    }

    public void updateDefaultOid(Long oid) {
        List<Long> uids = userExtMapper.uidsByOid(oid);
        uids.forEach(uid -> {
            Long defaultOid = null;
            if (AuthUtils.isSysAdmin(uid)) {
                defaultOid = userExtMapper.newDefaultOidForAdmin();
            } else {
                defaultOid = userExtMapper.newDefaultOid(uid);
            }
            userExtMapper.updateDefaultOid(uid, oid);
        });
    }
}
