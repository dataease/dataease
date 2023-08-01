package io.dataease.xpack.permissions.user.manage;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.dataease.api.permissions.role.dto.UserRequest;
import io.dataease.api.permissions.user.dto.UserCreator;
import io.dataease.api.permissions.user.dto.UserEditor;
import io.dataease.api.permissions.user.vo.*;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.exception.DEException;
import io.dataease.request.BaseGridRequest;
import io.dataease.request.ConditionEntity;
import io.dataease.utils.*;
import io.dataease.xpack.permissions.org.bo.PerOrgItem;
import io.dataease.xpack.permissions.org.manage.OrgPageManage;
import io.dataease.xpack.permissions.user.dao.auto.entity.PerUser;
import io.dataease.xpack.permissions.user.dao.auto.mapper.PerUserMapper;
import io.dataease.xpack.permissions.user.dao.ext.entity.UserRolePO;
import io.dataease.xpack.permissions.user.dao.ext.mapper.UserExtMapper;
import io.dataease.xpack.permissions.user.entity.UserSortEntity;
import io.dataease.xpack.permissions.utils.PerTokenUtils;
import io.dataease.xpack.permissions.utils.TokenCacheUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
@Lazy
@Transactional
public class UserPageManage {

    @Resource
    private PerUserMapper perUserMapper;

    @Resource
    private UserExtMapper userExtMapper;

    @Resource
    private RoleManage roleManage;

    @Resource
    private OrgPageManage orgPageManage;



    @Value("${dataease.default-pwd:DataEase123456}")
    private String DEFAULT_PWD;


    private Boolean stateFromCondition(BaseGridRequest request) {
        if (CollectionUtil.isEmpty(request.getConditions())) return null;
        List<ConditionEntity> roleConditions = request.getConditions().stream().filter(item -> StringUtils.equals("status", item.getField()) && ObjectUtils.isNotEmpty(item.getValue())).toList();
        if (CollectionUtil.isEmpty(roleConditions) || ((List) roleConditions.get(0).getValue()).size() != 1)
            return null;
        ConditionEntity conditionEntity = roleConditions.stream().findFirst().orElse(null);
        if (ObjectUtils.isEmpty(conditionEntity)) return null;
        return (Boolean) ((List) conditionEntity.getValue()).get(0);
    }

    private UserSortEntity userSortFromCondition(BaseGridRequest request) {
        List<String> orders = request.getOrders();
        if (CollectionUtil.isEmpty(orders))
            return null;
        String orderStr = orders.get(0);
        String[] arr = orderStr.split(" ");
        if (ArrayUtil.isEmpty(arr)) return null;
        String columnName = arr[0].trim();
        if (StringUtils.isBlank(columnName)) {
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
        Boolean state = null;
        wrapper.eq(ObjectUtils.isNotEmpty(state = stateFromCondition(request)), "u.enable", state);

        wrapper.like(StringUtils.isNotBlank(keyword), "u.name", keyword);
        UserSortEntity userSortEntity = userSortFromCondition(request);
        if (ObjectUtils.isEmpty(userSortEntity)) {
            userSortEntity = new UserSortEntity();
            userSortEntity.setColumnName("u.create_time");
            userSortEntity.setAsc(false);
        }
        wrapper.orderBy(true, userSortEntity.isAsc(), userSortEntity.getColumnName());
        IPage<UserGridVO> pager = userExtMapper.pager(page, wrapper);
        List<UserGridVO> records = null;
        if (CollectionUtil.isNotEmpty(records = pager.getRecords())) {
            List<UserRolePO> rolePOS = userExtMapper.userRole(records.stream().map(UserGridVO::getId).toList());
            if (CollectionUtil.isNotEmpty(rolePOS)) {
                Map<Long, List<UserRolePO>> listMap = rolePOS.stream().collect(Collectors.groupingBy(UserRolePO::getUid));
                records.forEach(record -> {
                    List<UserRolePO> userRolePOS = listMap.get(record.getId());
                    if (CollectionUtil.isNotEmpty(userRolePOS)) {
                        record.setRoleItems(userRolePOS.stream().map(item -> item.convert()).toList());
                    }
                });
            }
        }
        return pager;
    }


    @CacheEvict(cacheNames = "user_count", key = "'de'")
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

    @CacheEvict(cacheNames = "user_count", key = "'de'")
    public void delete(Long id) {
        Long oid = AuthUtils.getUser().getDefaultOid();
        CacheUtils.remove("user_roles", id.toString() + oid, t -> {
            proxy().deleteLogic(id, oid);
        });
    }

    @CacheEvict(cacheNames = "user_count", key = "'de'")
    public void batchDel(List<Long> ids) {
        Long oid = AuthUtils.getUser().getDefaultOid();
        List<String> keys = ids.stream().map(id -> id.toString() + oid).collect(Collectors.toList());
        CacheUtils.remove("user_roles", keys, t -> {
            proxy().batchDelLogic(ids, oid);
        });
    }

    private UserPageManage proxy() {
        return CommonBeanFactory.getBean(UserPageManage.class);
    }

    @Transactional
    public void batchDelLogic(List<Long> ids, Long oid) {
        for (Long id : ids) {
            deleteLogic(id, oid);
        }
    }

    @Transactional
    public void deleteLogic(Long id, Long oid) {
        int rCount = userExtMapper.userRoleMappingCount(id);
        if (rCount > 0) {
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

    public List<UserItemVO> optionForRole(UserRequest request) {
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

    public IPage<UserItemVO> selectedWithRole(Page<UserItemVO> page, UserRequest request) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("pur.rid", request.getRid());
        if (StringUtils.isNotBlank(request.getKeyword())) {
            queryWrapper.and((Consumer<QueryWrapper>) wrapper -> {
                wrapper.like("pu.name", request.getKeyword());
                wrapper.or();
                wrapper.like("pu.account", request.getKeyword());
            });
        }
        if (StringUtils.isNotBlank(request.getOrder())) {
            queryWrapper.orderBy(true, request.getOrder().split(" ").length <= 1 || request.getOrder().split(" ")[1].equalsIgnoreCase("asc"), request.getOrder().split(" ")[0]);
        }
        queryWrapper.orderByDesc("pu.create_time");
        IPage<UserItemVO> iPage = userExtMapper.selectedWithRole(page, queryWrapper);
        return iPage;
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

    @CacheEvict(cacheNames = "user_count", key = "'de'")
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
        return PerTokenUtils.generate(tokenUserBO, perUser.getPwd());
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

    @Cacheable(cacheNames = "user_count", key = "'de'")
    public int userCount() {
        QueryWrapper<PerUser> queryWrapper = new QueryWrapper<>();
        return perUserMapper.selectCount(queryWrapper).intValue();
    }

    public void switchLang(Long id, String lang) {
        userExtMapper.updateLanguage(id, lang);
    }


    public String defaultPwd() {
        return DEFAULT_PWD;
    }

    public void resetPwd(Long uid) {
        PerUser user = null;
        if (ObjectUtils.isNotEmpty(user = perUserMapper.selectById(uid))) {
            user.setPwd(Md5Utils.md5(DEFAULT_PWD));
            perUserMapper.updateById(user);
            if (Objects.requireNonNull(AuthUtils.getUser()).getUserId().equals(uid)) {
                TokenCacheUtils.del(uid, ServletUtils.getToken());
            }
        }
        DEException.throwException("user does not exist");
    }
}
