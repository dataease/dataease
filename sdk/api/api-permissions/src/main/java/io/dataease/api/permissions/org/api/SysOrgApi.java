package io.dataease.api.permissions.org.api;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.permissions.org.dto.SysOrgMemberAddRequest;
import io.dataease.api.permissions.org.dto.SysOrgMemberRequest;
import io.dataease.api.permissions.org.dto.SysOrgMemberRoleSwitchRequest;
import io.dataease.api.permissions.org.dto.SysOrgRoleOptionRequest;
import io.dataease.api.permissions.org.vo.SysOrgMemberVO;
import io.dataease.api.permissions.org.vo.SysOrgRoleOptionVO;
import io.dataease.auth.DeApiPath;
import io.dataease.model.KeywordRequest;
import io.dataease.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static io.dataease.constant.AuthResourceEnum.ORG;

@Tag(name = "系统组织管理")
@ApiSupport(order = 887, author = "fit2cloud-someone")
@DeApiPath(value = "/sys/org", rt = ORG)
public interface SysOrgApi {

    @Operation(summary = "分页查询组织成员")
    @PostMapping("/member/page")
    PageResult<SysOrgMemberVO> memberPage(@RequestBody SysOrgMemberRequest request);

    @Operation(summary = "查询候选用户（不属于任何组织的用户）")
    @PostMapping("/member/candidates")
    List<SysOrgMemberVO> memberCandidates(@RequestBody KeywordRequest request);

    @Operation(summary = "查询组织可选角色列表")
    @PostMapping("/member/roleOptions")
    List<SysOrgRoleOptionVO> memberRoleOptions(@RequestBody SysOrgRoleOptionRequest request);

    @Operation(summary = "添加组织成员")
    @PostMapping("/member/add")
    void addMembers(@RequestBody SysOrgMemberAddRequest request);

    @Operation(summary = "移除组织成员")
    @PostMapping("/member/remove/{userId}/{orgId}")
    void removeMember(@PathVariable("userId") Long userId, @PathVariable("orgId") Long orgId);

    @Operation(summary = "切换成员角色")
    @PostMapping("/member/switchRole")
    void switchRole(@RequestBody SysOrgMemberRoleSwitchRequest request);
}
