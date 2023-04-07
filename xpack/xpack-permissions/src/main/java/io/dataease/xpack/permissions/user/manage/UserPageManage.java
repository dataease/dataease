package io.dataease.xpack.permissions.user.manage;

import cn.hutool.core.collection.CollectionUtil;
import io.dataease.api.permissions.role.dto.UserRequest;
import io.dataease.api.permissions.user.dto.UserCreator;
import io.dataease.api.permissions.user.dto.UserEditor;
import io.dataease.api.permissions.user.vo.CurUserVO;
import io.dataease.api.permissions.user.vo.UserItem;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.exception.DEException;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import io.dataease.xpack.permissions.org.bo.PerOrgItem;
import io.dataease.xpack.permissions.org.manage.OrgPageManage;
import io.dataease.xpack.permissions.user.dao.auto.entity.PerUser;
import io.dataease.xpack.permissions.user.dao.auto.mapper.PerUserMapper;
import io.dataease.xpack.permissions.user.dao.ext.entity.UserInfoPO;
import io.dataease.xpack.permissions.user.dao.ext.mapper.UserExtMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserPageManage {

    @Resource
    private PerUserMapper perUserMapper;

    @Resource
    private UserExtMapper userExtMapper;

    @Resource
    private OrgPageManage orgPageManage;

    private static final String DEFAULT_PWD = "";

    public void save(UserCreator creator) {
        TokenUserBO user = AuthUtils.getUser();
        PerUser perUser = BeanUtils.copyBean(new PerUser(), creator);
        perUser.setCreateTime(System.currentTimeMillis());
        perUser.setId(IDUtils.snowID());
        perUser.setDefaultOid(user.getDefaultOid());
        perUser.setCreatorId(user.getUserId());
        perUser.setFrom(0);
        perUser.setLanguage("zh-CN");
        perUser.setPwd(DEFAULT_PWD);
        perUserMapper.insert(perUser);
    }

    public void edit(UserEditor editor) {
        PerUser perUser = BeanUtils.copyBean(new PerUser(), editor);
        perUserMapper.updateById(perUser);
    }

    public void delete(Long id) {
        int rCount = userExtMapper.userRoleMappingCount(id);
        if (rCount > 0) {
            Long oid = AuthUtils.getUser().getDefaultOid();
            userExtMapper.deleteUserRoleMapping(id, oid);
        }
        rCount = userExtMapper.userRoleMappingCount(id);
        if (rCount == 0) {
            perUserMapper.deleteById(id);
        }
    }

    public List<UserItem> optionForRole(UserRequest request) {
        Long defaultOid = AuthUtils.getUser().getDefaultOid();
        return userExtMapper.optionForRole(request.getRId(), defaultOid, request.getKeyword());
    }

    public List<UserItem> selectedForRole(UserRequest request) {
        Long defaultOid = AuthUtils.getUser().getDefaultOid();
        return userExtMapper.selectedForRole(request.getRId(), defaultOid, request.getKeyword());
    }

    public void switchOrg(Long oId) {
        TokenUserBO user = AuthUtils.getUser();
        List<PerOrgItem> perOrgItems = orgPageManage.queryByUser(user.getUserId());
        if (CollectionUtil.isNotEmpty(perOrgItems) && perOrgItems.stream().filter(item -> !item.isDisabled()).anyMatch(item -> item.getId() == oId)) {
            userExtMapper.switchOrg(user.getUserId(), oId);
            return;
        }
        DEException.throwException("invalid orgid");
    }

    public CurUserVO getUserInfo() {
        Long userId = AuthUtils.getUser().getUserId();
        return userExtMapper.userInfo(userId);
    }
}
