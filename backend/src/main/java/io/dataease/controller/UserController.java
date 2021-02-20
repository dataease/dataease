package io.dataease.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.dataease.base.domain.User;
import io.dataease.commons.constants.RoleConstants;
import io.dataease.commons.exception.MSException;
import io.dataease.commons.user.SessionUser;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.commons.utils.SessionUtils;
import io.dataease.controller.request.member.AddMemberRequest;
import io.dataease.controller.request.member.EditPassWordRequest;
import io.dataease.controller.request.member.QueryMemberRequest;
import io.dataease.controller.request.member.UserRequest;
import io.dataease.controller.request.organization.AddOrgMemberRequest;
import io.dataease.controller.request.organization.QueryOrgMemberRequest;
import io.dataease.dto.UserDTO;
import io.dataease.dto.UserRoleDTO;
import io.dataease.i18n.Translator;
import io.dataease.service.OrganizationService;
import io.dataease.service.UserService;
import io.dataease.service.WorkspaceService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("user")
@RestController
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private OrganizationService organizationService;
    @Resource
    private WorkspaceService workspaceService;

    @PostMapping("/special/add")
    @RequiresRoles(RoleConstants.ADMIN)
    public UserDTO insertUser(@RequestBody UserRequest user) {
        return userService.insert(user);
    }

    @PostMapping("/special/list/{goPage}/{pageSize}")
    @RequiresRoles(RoleConstants.ADMIN)
    public Pager<List<User>> getUserList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody io.dataease.controller.request.UserRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, userService.getUserListWithRequest(request));
    }

    @GetMapping("/special/user/role/{userId}")
    @RequiresRoles(RoleConstants.ADMIN)
    public UserRoleDTO getUserRole(@PathVariable("userId") String userId) {
        return userService.getUserRole(userId);
    }

    @GetMapping("/special/delete/{userId}")
    @RequiresRoles(RoleConstants.ADMIN)
    public void deleteUser(@PathVariable(value = "userId") String userId) {
        userService.deleteUser(userId);
        // 踢掉在线用户
        SessionUtils.kickOutUser(userId);
    }

    @PostMapping("/special/update")
    @RequiresRoles(RoleConstants.ADMIN)
    public void updateUser(@RequestBody UserRequest user) {
        userService.updateUserRole(user);
    }

    @PostMapping("/special/update_status")
    @RequiresRoles(RoleConstants.ADMIN)
    public void updateStatus(@RequestBody User user) {
        userService.updateUser(user);
    }

    @PostMapping("/special/ws/member/list/{goPage}/{pageSize}")
    @RequiresRoles(RoleConstants.ADMIN)
    public Pager<List<User>> getMemberListByAdmin(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryMemberRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, userService.getMemberList(request));
    }

    @PostMapping("/special/ws/member/list/all")
    @RequiresRoles(RoleConstants.ADMIN)
    public List<User> getMemberListByAdmin(@RequestBody QueryMemberRequest request) {
        return userService.getMemberList(request);
    }

    @PostMapping("/special/ws/member/add")
    @RequiresRoles(RoleConstants.ADMIN)
    public void addMemberByAdmin(@RequestBody AddMemberRequest request) {
        userService.addMember(request);
    }

    @GetMapping("/special/ws/member/delete/{workspaceId}/{userId}")
    @RequiresRoles(RoleConstants.ADMIN)
    public void deleteMemberByAdmin(@PathVariable String workspaceId, @PathVariable String userId) {
        userService.deleteMember(workspaceId, userId);
    }

    @PostMapping("/special/org/member/add")
    @RequiresRoles(RoleConstants.ADMIN)
    public void addOrganizationMemberByAdmin(@RequestBody AddOrgMemberRequest request) {
        userService.addOrganizationMember(request);
    }

    @GetMapping("/special/org/member/delete/{organizationId}/{userId}")
    @RequiresRoles(RoleConstants.ADMIN)
    public void delOrganizationMemberByAdmin(@PathVariable String organizationId, @PathVariable String userId) {
        userService.delOrganizationMember(organizationId, userId);
    }

    @PostMapping("/special/org/member/list/{goPage}/{pageSize}")
    @RequiresRoles(RoleConstants.ADMIN)
    public Pager<List<User>> getOrgMemberListByAdmin(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryOrgMemberRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, userService.getOrgMemberList(request));
    }

    @PostMapping("/special/org/member/list/all")
    @RequiresRoles(RoleConstants.ADMIN)
    public List<User> getOrgMemberListByAdmin(@RequestBody QueryOrgMemberRequest request) {
        return userService.getOrgMemberList(request);
    }

    @GetMapping("/list")
    public List<User> getUserList() {
        return userService.getUserList();
    }

    @PostMapping("/update/current")
    public UserDTO updateCurrentUser(@RequestBody User user) {
        String currentUserId = SessionUtils.getUserId();
        if (!StringUtils.equals(currentUserId, user.getId())) {
            MSException.throwException(Translator.get("not_authorized"));
        }
        userService.updateUser(user);
        UserDTO userDTO = userService.getUserDTO(user.getId());
        SessionUtils.putUser(SessionUser.fromUser(userDTO));
        return SessionUtils.getUser();
    }

    @PostMapping("/switch/source/org/{sourceId}")
    @RequiresRoles(value = {RoleConstants.ORG_ADMIN, RoleConstants.TEST_MANAGER, RoleConstants.TEST_VIEWER, RoleConstants.TEST_USER}, logical = Logical.OR)
    public UserDTO switchOrganization(@PathVariable(value = "sourceId") String sourceId) {
        userService.switchUserRole("organization", sourceId);
        return SessionUtils.getUser();
    }

    @PostMapping("/switch/source/ws/{sourceId}")
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_VIEWER, RoleConstants.TEST_USER}, logical = Logical.OR)
    public UserDTO switchWorkspace(@PathVariable(value = "sourceId") String sourceId) {
        userService.switchUserRole("workspace", sourceId);
        return SessionUtils.getUser();
    }

    @PostMapping("/refresh/{sign}/{sourceId}")
    public UserDTO refreshSessionUser(@PathVariable String sign, @PathVariable String sourceId) {
        userService.refreshSessionUser(sign, sourceId);
        return SessionUtils.getUser();
    }

    @GetMapping("/info/{userId}")
    public UserDTO getUserInfo(@PathVariable(value = "userId") String userId) {
        if (!StringUtils.equals(userId, SessionUtils.getUserId())) {
            MSException.throwException(Translator.get("not_authorized"));
        }
        return userService.getUserInfo(userId);
    }

    /**
     * 获取工作空间成员用户
     */
    @PostMapping("/ws/member/list/{goPage}/{pageSize}")
    @RequiresRoles(value = {RoleConstants.ORG_ADMIN, RoleConstants.TEST_MANAGER,
            RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
    public Pager<List<User>> getMemberList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryMemberRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, userService.getMemberList(request));
    }

    /**
     * 获取工作空间成员用户 不分页
     */
    @PostMapping("/ws/member/list/all")
    @RequiresRoles(value = {RoleConstants.ORG_ADMIN, RoleConstants.TEST_MANAGER,
            RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
    public List<User> getMemberList(@RequestBody QueryMemberRequest request) {
        return userService.getMemberList(request);
    }

    /**
     * 添加工作空间成员
     */
    @PostMapping("/ws/member/add")
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.ORG_ADMIN}, logical = Logical.OR)
    public void addMember(@RequestBody AddMemberRequest request) {
        String wsId = request.getWorkspaceId();
        workspaceService.checkWorkspaceOwner(wsId);
        userService.addMember(request);
    }

    /**
     * 删除工作空间成员
     */
    @GetMapping("/ws/member/delete/{workspaceId}/{userId}")
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.ORG_ADMIN}, logical = Logical.OR)
    public void deleteMember(@PathVariable String workspaceId, @PathVariable String userId) {
        workspaceService.checkWorkspaceOwner(workspaceId);
        String currentUserId = SessionUtils.getUser().getId();
        if (StringUtils.equals(userId, currentUserId)) {
            MSException.throwException(Translator.get("cannot_remove_current"));
        }
        userService.deleteMember(workspaceId, userId);
    }

    /**
     * 添加组织成员
     */
    @PostMapping("/org/member/add")
    @RequiresRoles(RoleConstants.ORG_ADMIN)
    public void addOrganizationMember(@RequestBody AddOrgMemberRequest request) {
        organizationService.checkOrgOwner(request.getOrganizationId());
        userService.addOrganizationMember(request);
    }

    /**
     * 删除组织成员
     */
    @GetMapping("/org/member/delete/{organizationId}/{userId}")
    @RequiresRoles(RoleConstants.ORG_ADMIN)
    public void delOrganizationMember(@PathVariable String organizationId, @PathVariable String userId) {
        organizationService.checkOrgOwner(organizationId);
        String currentUserId = SessionUtils.getUser().getId();
        if (StringUtils.equals(userId, currentUserId)) {
            MSException.throwException(Translator.get("cannot_remove_current"));
        }
        userService.delOrganizationMember(organizationId, userId);
    }

    /**
     * 查询组织成员列表
     */
    @PostMapping("/org/member/list/{goPage}/{pageSize}")
    @RequiresRoles(value = {RoleConstants.ORG_ADMIN, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public Pager<List<User>> getOrgMemberList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryOrgMemberRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, userService.getOrgMemberList(request));
    }

    /**
     * 组织成员列表不分页
     */
    @PostMapping("/org/member/list/all")
    @RequiresRoles(value = {RoleConstants.ORG_ADMIN, RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public List<User> getOrgMemberList(@RequestBody QueryOrgMemberRequest request) {
        return userService.getOrgMemberList(request);
    }

    @GetMapping("/besideorg/list/{orgId}")
    public List<User> getBesideOrgMemberList(@PathVariable String orgId) {
        return userService.getBesideOrgMemberList(orgId);
    }

    /*
     * 修改当前用户密码
     * */
    @PostMapping("/update/password")
    public int updateCurrentUserPassword(@RequestBody EditPassWordRequest request) {
        return userService.updateCurrentUserPassword(request);
    }

    /*管理员修改用户密码*/
    @PostMapping("/special/password")
    public int updateUserPassword(@RequestBody EditPassWordRequest request) {
        return userService.updateUserPassword(request);
    }

    /**
     * 获取工作空间成员用户 不分页
     */
    @PostMapping("/ws/member/tester/list")
    @RequiresRoles(value = {RoleConstants.ORG_ADMIN, RoleConstants.TEST_MANAGER,
            RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
    public List<User> getTestManagerAndTestUserList(@RequestBody QueryMemberRequest request) {
        return userService.getTestManagerAndTestUserList(request);
    }

    @GetMapping("/search/{condition}")
    @RequiresRoles(value = {RoleConstants.ADMIN, RoleConstants.ORG_ADMIN, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public List<User> searchUser(@PathVariable String condition) {
        return userService.searchUser(condition);
    }

}
