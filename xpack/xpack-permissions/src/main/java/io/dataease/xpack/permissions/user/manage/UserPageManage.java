package io.dataease.xpack.permissions.user.manage;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.dataease.api.permissions.role.dto.UserRequest;
import io.dataease.api.permissions.user.dto.UserCreator;
import io.dataease.api.permissions.user.dto.UserEditor;
import io.dataease.api.permissions.user.vo.CurUserVO;
import io.dataease.api.permissions.user.vo.UserFormVO;
import io.dataease.api.permissions.user.vo.UserGridVO;
import io.dataease.api.permissions.user.vo.UserItem;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.exception.DEException;
import io.dataease.request.BaseGridRequest;
import io.dataease.request.ConditionEntity;
import io.dataease.utils.*;
import io.dataease.xpack.permissions.org.bo.PerOrgItem;
import io.dataease.xpack.permissions.org.manage.OrgPageManage;
import io.dataease.xpack.permissions.user.dao.auto.entity.PerUser;
import io.dataease.xpack.permissions.user.dao.auto.mapper.PerUserMapper;
import io.dataease.xpack.permissions.user.dao.ext.mapper.UserExtMapper;
import io.dataease.xpack.permissions.user.entity.UserSortEntity;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
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

    @Resource
    private OrgPageManage orgPageManage;


    private static final String DEFAULT_PWD = "DataEase123456";

    private List<Long> roleIdsFromCondition(BaseGridRequest request) {
        if (CollectionUtil.isEmpty(request.getConditions())) return null;
        List<ConditionEntity> roleConditions = request.getConditions().stream().filter(item -> StringUtils.equals("rid", item.getField()) && StringUtils.equals("in", item.getOperator()) && ObjectUtils.isNotEmpty(item.getValue())).toList();
        ConditionEntity conditionEntity = roleConditions.stream().findFirst().orElseGet(null);
        if (ObjectUtils.isEmpty(conditionEntity)) return null;
        return ((List) conditionEntity.getValue()).stream().map(item -> Long.parseLong(item.toString())).toList();
    }

    private Boolean stateFromCondition(BaseGridRequest request) {
        if (CollectionUtil.isEmpty(request.getConditions())) return null;
        List<ConditionEntity> roleConditions = request.getConditions().stream().filter(item -> StringUtils.equals("state", item.getField()) && StringUtils.equals("eq", item.getOperator()) && ObjectUtils.isNotEmpty(item.getValue())).toList();
        ConditionEntity conditionEntity = roleConditions.stream().findFirst().orElse(null);
        if (ObjectUtils.isEmpty(conditionEntity)) return null;
        return (Boolean) conditionEntity.getValue();
    }

    private UserSortEntity userSortFromCondition(BaseGridRequest request) {
        List<String> orders = request.getOrders();
        if (CollectionUtil.isEmpty(orders))
            return null;
        String orderStr = orders.get(0);
        String[] arr = orderStr.split(" ");
        if (ArrayUtil.isEmpty(arr)) return null;
        String columnName = arr[0].trim();
        if (StringUtils.isBlank(columnName) || !StringUtils.equals("u.create_time", columnName)) {
            return null;
        }
        boolean isAsc = true;
        if (arr.length > 1 && StringUtils.isNotBlank(arr[1].trim()) && StringUtils.equalsIgnoreCase("desc", arr[1].trim())) {
            isAsc = false;
        }
        UserSortEntity userSortEntity = new UserSortEntity();
        userSortEntity.setAsc(isAsc);
        userSortEntity.setColumnName(columnName);
        return userSortEntity;
    }

    public IPage<UserGridVO> pager(Page<UserGridVO> page, BaseGridRequest request) {
        Long oid = AuthUtils.getUser().getDefaultOid();
        QueryWrapper<UserGridVO> wrapper = new QueryWrapper<>();
        String keyword = request.getKeyword();
        wrapper.eq("pur.oid", oid);
        List<Long> rids = null;
        Boolean state = null;
        wrapper.eq(ObjectUtils.isNotEmpty(state = stateFromCondition(request)), "u.enable", state);
        wrapper.in(CollectionUtil.isNotEmpty(rids = roleIdsFromCondition(request)), "pur.rid", rids);

        wrapper.like(StringUtils.isNotBlank(keyword), "u.name", keyword);
        UserSortEntity userSortEntity = userSortFromCondition(request);
        if (ObjectUtils.isEmpty(userSortEntity)) {
            userSortEntity = new UserSortEntity();
            userSortEntity.setColumnName("u.create_time");
            userSortEntity.setAsc(false);
        }
        wrapper.orderBy(true, userSortEntity.isAsc(), userSortEntity.getColumnName());
        IPage<UserGridVO> pager = userExtMapper.pager(page, wrapper);
        return pager;
    }

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
        return userExtMapper.optionForRole(defaultOid, request.getRid(), request.getKeyword());
    }

    public List<UserItem> selectedForRole(UserRequest request) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("pur.rid", request.getRid());
        queryWrapper.like(StringUtils.isNotBlank(request.getKeyword()), "pu", request.getKeyword());
        List<UserItem> result = userExtMapper.selectedForRole(queryWrapper);
        return result;
    }

    public void switchOrg(Long uid, Long oId) {
        if (AuthUtils.isSysAdmin(uid)) {
            userExtMapper.switchOrg(uid, oId);
            return;
        }
        List<PerOrgItem> perOrgItems = orgPageManage.queryByUser(uid, null);
        if (CollectionUtil.isNotEmpty(perOrgItems) && perOrgItems.stream().filter(item -> !item.isDisabled()).anyMatch(item -> item.getId().equals(oId))) {
            userExtMapper.switchOrg(uid, oId);
            return;
        }
        DEException.throwException("invalid orgid");
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

    public String switchToken() {
        Long userId = AuthUtils.getUser().getUserId();
        PerUser perUser = perUserMapper.selectById(userId);
        TokenUserBO tokenUserBO = new TokenUserBO();
        tokenUserBO.setUserId(userId);
        tokenUserBO.setDefaultOid(perUser.getDefaultOid());
        return TokenUtils.generate(tokenUserBO, perUser.getPwd());
    }

    public List<UserItem> queryForOrg(String keyword, Long oid) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("pur.oid", oid);
        queryWrapper.like(StringUtils.isNotBlank(keyword), "pu", keyword);
        List<UserItem> result = userExtMapper.selectedForRole(queryWrapper);
        return result.stream().filter(item -> !AuthUtils.isSysAdmin(item.getId())).toList();
    }

    public List<Long> uidsForAdmin(Long oid) {
        return userExtMapper.uidsForAdmin(oid);
    }
    public List<Long> uidsForReadonly(Long oid) {
        return userExtMapper.uidsForReadonly(oid);
    }
}
