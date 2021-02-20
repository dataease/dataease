package io.dataease.service;

import io.dataease.base.domain.*;
import io.dataease.base.mapper.*;
import io.dataease.base.mapper.ext.ExtUserMapper;
import io.dataease.base.mapper.ext.ExtUserRoleMapper;
import io.dataease.commons.constants.RoleConstants;
import io.dataease.commons.constants.UserSource;
import io.dataease.commons.constants.UserStatus;
import io.dataease.commons.exception.MSException;
import io.dataease.commons.user.SessionUser;
import io.dataease.commons.utils.CodingUtil;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.commons.utils.SessionUtils;
import io.dataease.controller.ResultHolder;
import io.dataease.controller.request.LoginRequest;
import io.dataease.controller.request.member.AddMemberRequest;
import io.dataease.controller.request.member.EditPassWordRequest;
import io.dataease.controller.request.member.QueryMemberRequest;
import io.dataease.controller.request.member.UserRequest;
import io.dataease.controller.request.organization.AddOrgMemberRequest;
import io.dataease.controller.request.organization.QueryOrgMemberRequest;
import io.dataease.dto.UserDTO;
import io.dataease.dto.UserRoleDTO;
import io.dataease.i18n.Translator;
import io.dataease.security.MsUserToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static io.dataease.commons.constants.SessionConstants.ATTR_USER;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private RoleMapper roleMapper;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private ExtUserRoleMapper extUserRoleMapper;
    @Resource
    private OrganizationMapper organizationMapper;
    @Resource
    private WorkspaceMapper workspaceMapper;
    @Resource
    private ExtUserMapper extUserMapper;
    @Lazy
    @Resource
    private WorkspaceService workspaceService;

    public UserDTO insert(UserRequest user) {
        checkUserParam(user);
        //
        String id = user.getId();
        User user1 = userMapper.selectByPrimaryKey(id);
        if (user1 != null) {
            MSException.throwException(Translator.get("user_id_already_exists"));
        } else {
            createUser(user);
        }
        List<Map<String, Object>> roles = user.getRoles();
        if (!roles.isEmpty()) {
            insertUserRole(roles, user.getId());
        }
        return getUserDTO(user.getId());
    }

    public User selectUser(String userId, String email) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            UserExample example = new UserExample();
            example.createCriteria().andEmailEqualTo(email);
            List<User> users = userMapper.selectByExample(example);
            if (!CollectionUtils.isEmpty(users)) {
                return users.get(0);
            }
        }
        return user;

    }

    private void insertUserRole(List<Map<String, Object>> roles, String userId) {
        for (int i = 0; i < roles.size(); i++) {
            Map<String, Object> map = roles.get(i);
            String role = (String) map.get("id");
            if (StringUtils.equals(role, RoleConstants.ADMIN)) {
                UserRole userRole = new UserRole();
                userRole.setId(UUID.randomUUID().toString());
                userRole.setUserId(userId);
                userRole.setUpdateTime(System.currentTimeMillis());
                userRole.setCreateTime(System.currentTimeMillis());
                userRole.setRoleId(role);
                // TODO 修改
                userRole.setSourceId("adminSourceId");
                userRoleMapper.insertSelective(userRole);
            } else {
//                if (!map.keySet().contains("ids")) {
//                    MSException.throwException(role + " no source id");
//                }
                List<String> list = (List<String>) map.get("ids");
                for (int j = 0; j < list.size(); j++) {
                    UserRole userRole1 = new UserRole();
                    userRole1.setId(UUID.randomUUID().toString());
                    userRole1.setUserId(userId);
                    userRole1.setRoleId(role);
                    userRole1.setUpdateTime(System.currentTimeMillis());
                    userRole1.setCreateTime(System.currentTimeMillis());
                    userRole1.setSourceId(list.get(j));
                    userRoleMapper.insertSelective(userRole1);
                }
            }
        }
    }

    private void checkUserParam(User user) {

        if (StringUtils.isBlank(user.getId())) {
            MSException.throwException(Translator.get("user_id_is_null"));
        }

        if (StringUtils.isBlank(user.getName())) {
            MSException.throwException(Translator.get("user_name_is_null"));
        }

        if (StringUtils.isBlank(user.getEmail())) {
            MSException.throwException(Translator.get("user_email_is_null"));
        }
        // password
    }

    public void createUser(User userRequest) {
        User user = new User();
        BeanUtils.copyProperties(userRequest, user);
        user.setCreateTime(System.currentTimeMillis());
        user.setUpdateTime(System.currentTimeMillis());
        // 默认1:启用状态
        user.setStatus(UserStatus.NORMAL);
        user.setSource(UserSource.LOCAL.name());
        // 密码使用 MD5
        user.setPassword(CodingUtil.md5(user.getPassword()));
        checkEmailIsExist(user.getEmail());
        userMapper.insertSelective(user);
    }

    public void addLdapUser(User user) {
        user.setCreateTime(System.currentTimeMillis());
        user.setUpdateTime(System.currentTimeMillis());
        user.setStatus(UserStatus.NORMAL);
        checkEmailIsExist(user.getEmail());
        userMapper.insertSelective(user);
    }

    public void createOssUser(User user) {
        user.setCreateTime(System.currentTimeMillis());
        user.setUpdateTime(System.currentTimeMillis());
        user.setStatus(UserStatus.NORMAL);
        if (StringUtils.isBlank(user.getEmail())) {
            user.setEmail(user.getId() + "@metershpere.io");
        }
        userMapper.insertSelective(user);
    }


    private void checkEmailIsExist(String email) {
        UserExample userExample = new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();
        criteria.andEmailEqualTo(email);
        List<User> userList = userMapper.selectByExample(userExample);
        if (!CollectionUtils.isEmpty(userList)) {
            MSException.throwException(Translator.get("user_email_already_exists"));
        }
    }

    public UserDTO getUserDTO(String userId) {

        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return null;
        }
        if (StringUtils.equals(user.getStatus(), UserStatus.DISABLED)) {
            throw new DisabledAccountException();
        }
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        UserRoleDTO userRole = getUserRole(userId);
        userDTO.setUserRoles(Optional.ofNullable(userRole.getUserRoles()).orElse(new ArrayList<>()));
        userDTO.setRoles(Optional.ofNullable(userRole.getRoles()).orElse(new ArrayList<>()));
        return userDTO;
    }

    public UserDTO getLoginUser(String userId, List<String> list) {
        UserExample example = new UserExample();
        example.createCriteria().andIdEqualTo(userId).andSourceIn(list);
        if (userMapper.countByExample(example) == 0) {
            return null;
        }
        return getUserDTO(userId);
    }

    public UserDTO getUserDTOByEmail(String email, String... source) {
        UserExample example = new UserExample();
        UserExample.Criteria criteria = example.createCriteria();
        criteria.andEmailEqualTo(email);

        if (!CollectionUtils.isEmpty(Arrays.asList(source))) {
            criteria.andSourceIn(Arrays.asList(source));
        }

        List<User> users = userMapper.selectByExample(example);

        if (users == null || users.size() <= 0) {
            return null;
        }

        return getUserDTO(users.get(0).getId());
    }

    public UserRoleDTO getUserRole(String userId) {
        UserRoleDTO userRoleDTO = new UserRoleDTO();
        //
        UserRoleExample userRoleExample = new UserRoleExample();
        userRoleExample.createCriteria().andUserIdEqualTo(userId);
        List<UserRole> userRoleList = userRoleMapper.selectByExample(userRoleExample);

        if (CollectionUtils.isEmpty(userRoleList)) {
            return userRoleDTO;
        }
        // 设置 user_role
        userRoleDTO.setUserRoles(userRoleList);

        List<String> roleIds = userRoleList.stream().map(UserRole::getRoleId).collect(Collectors.toList());

        RoleExample roleExample = new RoleExample();
        roleExample.createCriteria().andIdIn(roleIds);

        List<Role> roleList = roleMapper.selectByExample(roleExample);
        userRoleDTO.setRoles(roleList);

        return userRoleDTO;
    }

    public List<User> getUserList() {
        UserExample example = new UserExample();
        example.setOrderByClause("update_time desc");
        return userMapper.selectByExample(example);
    }

    public List<User> getUserListWithRequest(io.dataease.controller.request.UserRequest request) {
        return extUserMapper.getUserList(request);
    }

    public void deleteUser(String userId) {
        SessionUser user = SessionUtils.getUser();
        if (StringUtils.equals(user.getId(), userId)) {
            MSException.throwException(Translator.get("cannot_delete_current_user"));
        }

        UserRoleExample example = new UserRoleExample();
        example.createCriteria().andUserIdEqualTo(userId);
        userRoleMapper.deleteByExample(example);

        userMapper.deleteByPrimaryKey(userId);
    }

    public void updateUserRole(UserRequest user) {
        String userId = user.getId();
        UserRoleExample userRoleExample = new UserRoleExample();
        userRoleExample.createCriteria().andUserIdEqualTo(userId);
        List<UserRole> userRoles = userRoleMapper.selectByExample(userRoleExample);
        List<String> list = userRoles.stream().map(UserRole::getSourceId).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(list)) {
            if (list.contains(user.getLastWorkspaceId()) || list.contains(user.getLastOrganizationId())) {
                user.setLastOrganizationId("");
                user.setLastWorkspaceId("");
                userMapper.updateByPrimaryKeySelective(user);
            }
        }

        userRoleMapper.deleteByExample(userRoleExample);
        List<Map<String, Object>> roles = user.getRoles();
        if (!roles.isEmpty()) {
            insertUserRole(roles, user.getId());
        }

        UserExample example = new UserExample();
        UserExample.Criteria criteria = example.createCriteria();
        criteria.andEmailEqualTo(user.getEmail());
        criteria.andIdNotEqualTo(user.getId());
        if (userMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("user_email_already_exists"));
        }

        user.setUpdateTime(System.currentTimeMillis());
        userMapper.updateByPrimaryKeySelective(user);
    }

    public void updateUser(User user) {
        // todo 提取重复代码
        if (StringUtils.isNotBlank(user.getEmail())) {
            UserExample example = new UserExample();
            UserExample.Criteria criteria = example.createCriteria();
            criteria.andEmailEqualTo(user.getEmail());
            criteria.andIdNotEqualTo(user.getId());
            if (userMapper.countByExample(example) > 0) {
                MSException.throwException(Translator.get("user_email_already_exists"));
            }
        }
        user.setPassword(null);
        user.setUpdateTime(System.currentTimeMillis());
        userMapper.updateByPrimaryKeySelective(user);
        // 禁用用户之后，剔除在线用户
        if (StringUtils.equals(user.getStatus(), UserStatus.DISABLED)) {
            SessionUtils.kickOutUser(user.getId());
        }
    }

    public void switchUserRole(String sign, String sourceId) {
        SessionUser sessionUser = SessionUtils.getUser();
        // 获取最新UserDTO
        UserDTO user = getUserDTO(sessionUser.getId());
        User newUser = new User();

        if (StringUtils.equals("organization", sign)) {
            user.setLastOrganizationId(sourceId);
            List<Workspace> workspaces = workspaceService.getWorkspaceListByOrgIdAndUserId(sourceId);
            if (workspaces.size() > 0) {
                user.setLastWorkspaceId(workspaces.get(0).getId());
            } else {
                user.setLastWorkspaceId("");
            }
        }
        if (StringUtils.equals("workspace", sign)) {
            Workspace workspace = workspaceMapper.selectByPrimaryKey(sourceId);
            user.setLastOrganizationId(workspace.getOrganizationId());
            user.setLastWorkspaceId(sourceId);
        }
        BeanUtils.copyProperties(user, newUser);
        // 切换工作空间或组织之后更新 session 里的 user
        SessionUtils.putUser(SessionUser.fromUser(user));
        userMapper.updateByPrimaryKeySelective(newUser);
    }

    public UserDTO getUserInfo(String userId) {
        return getUserDTO(userId);
    }

    public List<User> getMemberList(QueryMemberRequest request) {
        return extUserRoleMapper.getMemberList(request);
    }

    public void addMember(AddMemberRequest request) {
        if (!CollectionUtils.isEmpty(request.getUserIds())) {
            for (String userId : request.getUserIds()) {
                UserRoleExample userRoleExample = new UserRoleExample();
                userRoleExample.createCriteria().andUserIdEqualTo(userId).andSourceIdEqualTo(request.getWorkspaceId());
                List<UserRole> userRoles = userRoleMapper.selectByExample(userRoleExample);
                if (userRoles.size() > 0) {
                    MSException.throwException(Translator.get("user_already_exists"));
                } else {
                    for (String roleId : request.getRoleIds()) {
                        UserRole userRole = new UserRole();
                        userRole.setRoleId(roleId);
                        userRole.setSourceId(request.getWorkspaceId());
                        userRole.setUserId(userId);
                        userRole.setId(UUID.randomUUID().toString());
                        userRole.setUpdateTime(System.currentTimeMillis());
                        userRole.setCreateTime(System.currentTimeMillis());
                        userRoleMapper.insertSelective(userRole);
                    }
                }
            }
        }
    }

    public void deleteMember(String workspaceId, String userId) {
        UserRoleExample example = new UserRoleExample();
        example.createCriteria().andRoleIdLike("%test%")
                .andUserIdEqualTo(userId).andSourceIdEqualTo(workspaceId);

        User user = userMapper.selectByPrimaryKey(userId);
        if (StringUtils.equals(workspaceId, user.getLastWorkspaceId())) {
            user.setLastWorkspaceId("");
            user.setLastOrganizationId("");
            userMapper.updateByPrimaryKeySelective(user);
        }

        userRoleMapper.deleteByExample(example);
    }

    public void addOrganizationMember(AddOrgMemberRequest request) {
        if (!CollectionUtils.isEmpty(request.getUserIds())) {
            for (String userId : request.getUserIds()) {
                UserRoleExample userRoleExample = new UserRoleExample();
                userRoleExample.createCriteria().andUserIdEqualTo(userId).andSourceIdEqualTo(request.getOrganizationId());
                List<UserRole> userRoles = userRoleMapper.selectByExample(userRoleExample);
                if (userRoles.size() > 0) {
                    MSException.throwException(Translator.get("user_already_exists") + ": " + userId);
                } else {
                    for (String roleId : request.getRoleIds()) {
                        UserRole userRole = new UserRole();
                        userRole.setId(UUID.randomUUID().toString());
                        userRole.setRoleId(roleId);
                        userRole.setSourceId(request.getOrganizationId());
                        userRole.setUserId(userId);
                        userRole.setUpdateTime(System.currentTimeMillis());
                        userRole.setCreateTime(System.currentTimeMillis());
                        userRoleMapper.insertSelective(userRole);
                    }
                }
            }
        }
    }

    public void delOrganizationMember(String organizationId, String userId) {

        List<String> resourceIds = workspaceService.getWorkspaceIdsOrgId(organizationId);
        resourceIds.add(organizationId);

        UserRoleExample userRoleExample = new UserRoleExample();
        userRoleExample.createCriteria().andUserIdEqualTo(userId).andSourceIdIn(resourceIds);

        User user = userMapper.selectByPrimaryKey(userId);
        if (StringUtils.equals(organizationId, user.getLastOrganizationId())) {
            user.setLastWorkspaceId("");
            user.setLastOrganizationId("");
            userMapper.updateByPrimaryKeySelective(user);
        }

        userRoleMapper.deleteByExample(userRoleExample);
    }

    public List<User> getOrgMemberList(QueryOrgMemberRequest request) {
        return extUserRoleMapper.getOrgMemberList(request);
    }

    public boolean checkUserPassword(String userId, String password) {
        if (StringUtils.isBlank(userId)) {
            MSException.throwException(Translator.get("user_name_is_null"));
        }
        if (StringUtils.isBlank(password)) {
            MSException.throwException(Translator.get("password_is_null"));
        }
        UserExample example = new UserExample();
        example.createCriteria().andIdEqualTo(userId).andPasswordEqualTo(CodingUtil.md5(password));
        return userMapper.countByExample(example) > 0;
    }

    /**
     * 查询该组织外的其他用户列表
     */
    public List<User> getBesideOrgMemberList(String orgId) {
        return extUserRoleMapper.getBesideOrgMemberList(orgId);
    }

    public void setLanguage(String lang) {
        if (SessionUtils.getUser() != null) {
            User user = new User();
            user.setId(SessionUtils.getUser().getId());
            user.setLanguage(lang);
            updateUser(user);
            SessionUtils.getUser().setLanguage(lang);
        }
    }

    public void refreshSessionUser(String sign, String sourceId) {
        SessionUser sessionUser = SessionUtils.getUser();
        // 获取最新UserDTO
        UserDTO user = getUserDTO(sessionUser.getId());
        User newUser = new User();
        if (StringUtils.equals("organization", sign) && StringUtils.equals(sourceId, user.getLastOrganizationId())) {
            user.setLastOrganizationId("");
            user.setLastWorkspaceId("");
        }
        if (StringUtils.equals("workspace", sign) && StringUtils.equals(sourceId, user.getLastWorkspaceId())) {
            user.setLastWorkspaceId("");
        }

        BeanUtils.copyProperties(user, newUser);

        SessionUtils.putUser(SessionUser.fromUser(user));
        userMapper.updateByPrimaryKeySelective(newUser);
    }


    /*修改当前用户用户密码*/
    private User updateCurrentUserPwd(EditPassWordRequest request) {
        String oldPassword = CodingUtil.md5(request.getPassword(), "utf-8");
        String newPassword = request.getNewpassword();
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdEqualTo(SessionUtils.getUser().getId()).andPasswordEqualTo(oldPassword);
        List<User> users = userMapper.selectByExample(userExample);
        if (!CollectionUtils.isEmpty(users)) {
            User user = users.get(0);
            user.setPassword(CodingUtil.md5(newPassword));
            user.setUpdateTime(System.currentTimeMillis());
            return user;
        }
        MSException.throwException(Translator.get("password_modification_failed"));
        return null;
    }

    public int updateCurrentUserPassword(EditPassWordRequest request) {
        User user = updateCurrentUserPwd(request);
        return extUserMapper.updatePassword(user);
    }

    /*管理员修改用户密码*/
    private User updateUserPwd(EditPassWordRequest request) {
        User user = userMapper.selectByPrimaryKey(request.getId());
        String newPassword = request.getNewpassword();
        user.setPassword(CodingUtil.md5(newPassword));
        user.setUpdateTime(System.currentTimeMillis());
        return user;
    }

    public int updateUserPassword(EditPassWordRequest request) {
        User user = updateUserPwd(request);
        return extUserMapper.updatePassword(user);
    }

    public String getDefaultLanguage() {
        final String key = "default.language";
        return extUserMapper.getDefaultLanguage(key);
    }

    public List<User> getTestManagerAndTestUserList(QueryMemberRequest request) {
        return extUserRoleMapper.getTestManagerAndTestUserList(request);
    }

    public ResultHolder login(LoginRequest request) {
        String login = (String) SecurityUtils.getSubject().getSession().getAttribute("authenticate");
        String username = StringUtils.trim(request.getUsername());
        String password = "";
        if (!StringUtils.equals(login, UserSource.LDAP.name())) {
            password = StringUtils.trim(request.getPassword());
            if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
                return ResultHolder.error("user or password can't be null");
            }
        }

        MsUserToken token = new MsUserToken(username, password, login);
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(token);
            if (subject.isAuthenticated()) {
                UserDTO user = (UserDTO) subject.getSession().getAttribute(ATTR_USER);
                // 自动选中组织，工作空间
                if (StringUtils.isEmpty(user.getLastOrganizationId())) {
                    List<UserRole> userRoles = user.getUserRoles();
                    List<UserRole> test = userRoles.stream().filter(ur -> ur.getRoleId().startsWith("test")).collect(Collectors.toList());
                    List<UserRole> org = userRoles.stream().filter(ur -> ur.getRoleId().startsWith("org")).collect(Collectors.toList());
                    if (test.size() > 0) {
                        String wsId = test.get(0).getSourceId();
                        switchUserRole("workspace", wsId);
                    } else if (org.size() > 0) {
                        String orgId = org.get(0).getSourceId();
                        switchUserRole("organization", orgId);
                    }
                }
                // 返回 userDTO
                return ResultHolder.success(subject.getSession().getAttribute("user"));
            } else {
                return ResultHolder.error(Translator.get("login_fail"));
            }
        } catch (ExcessiveAttemptsException e) {
            throw new ExcessiveAttemptsException(Translator.get("excessive_attempts"));
        } catch (LockedAccountException e) {
            throw new LockedAccountException(Translator.get("user_locked"));
        } catch (DisabledAccountException e) {
            throw new DisabledAccountException(Translator.get("user_has_been_disabled"));
        } catch (ExpiredCredentialsException e) {
            throw new ExpiredCredentialsException(Translator.get("user_expires"));
        } catch (AuthenticationException e) {
            throw new AuthenticationException(e.getMessage());
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException(Translator.get("not_authorized") + e.getMessage());
        }
    }

    public List<User> searchUser(String condition) {
        return extUserMapper.searchUser(condition);
    }

    public void logout() throws Exception {
        SSOService ssoService = CommonBeanFactory.getBean(SSOService.class);
        if (ssoService != null) {
            ssoService.logout();
        }
    }
}
