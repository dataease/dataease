package io.dataease.api.permissions.auth.api;


import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.permissions.auth.dto.*;
import io.dataease.api.permissions.auth.vo.PermissionValVO;
import io.dataease.api.permissions.auth.vo.ResourceNodeVO;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import io.dataease.model.ExportTaskDTO;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "内部资源交互")
@ApiSupport(order = 998)
@Hidden
public interface InteractiveAuthApi {

    /** Internal target authorization, deliberately unmapped. Old extensions and null/AOP returns deny. */
    default Boolean checkAiAuditAccess(Long actorId, Long currentOrganizationId, Long proxyOrganizationId, Long auditOrganizationId) {
        return Boolean.FALSE;
    }

    /** 社区模式内部账号检查；复用已有认证身份，不检查企业菜单或组织。 */
    default void checkAiCommunityAccess(Long actorId) {
        throw new UnsupportedOperationException("AI community identity unavailable");
    }

    /** 全局 AI 配置目录的最小投影；无 HTTP 路由，不改变组织管理及业务资源授权。 */
    record AiOrganization(Long id, String name, Long parentId) { }
    default List<AiOrganization> aiOrganizations(Long actorId, Long organizationId, Long proxyOrganizationId) {
        throw new UnsupportedOperationException("AI organization directory unavailable");
    }

    /** 内部 Java 调用：按明确主体校验 AI 身份及可选配置菜单使用权，不暴露 HTTP 路由。 */
    default void checkAiAccess(Long actorId, Long organizationId, Long proxyOrganizationId, boolean configuration) {
        throw new UnsupportedOperationException("AI authorization unavailable");
    }

    @Operation(summary = "查询菜单ID")
    @ApiOperationSupport(order = 1)
    @GetMapping("/menuIds")
    List<Long> menuIds();


    @Operation(summary = "查询资源树")
    @ApiOperationSupport(order = 2)
    @PostMapping("/resource")
    List<BusiNodeVO> resource(@RequestBody BusiNodeRequest request);

    /**
     * 下面3个接口为内部调用接口不对外开放
     *
     * @param creator
     */

    @Operation(summary = "同步保存资源")
    @ApiOperationSupport(order = 3)
    @PostMapping("/resource/create")
    void saveResource(@RequestBody BusiResourceCreator creator);

    @Operation(summary = "同步更新资源")
    @ApiOperationSupport(order = 4)
    @PostMapping("/resource/edit")
    void editResource(@RequestBody BusiResourceEditor editor);

    @Operation(summary = "同步删除资源")
    @ApiOperationSupport(order = 5)
    @GetMapping("/resource/del/{id}")
    void delResource(@PathVariable("id") Long id);

    @Operation(summary = "删除检测")
    @ApiOperationSupport(order = 6)
    @GetMapping("/resource/checkDel/{id}")
    boolean checkDel(@PathVariable("id") Long id);

    @Operation(summary = "移动资源")
    @ApiOperationSupport(order = 7)
    @PostMapping("/moveResource")
    void moveResource(@RequestBody BusiResourceMover mover);

    @Operation(summary = "权限校验")
    @ApiOperationSupport(order = 8)
    @PostMapping("/checkAuth")
    void checkAuth(@RequestBody BusiPerCheckDTO checkDTO);

    /**
     * Internal share-owner authorization. The subject is supplied by trusted share storage,
     * never installed as the request identity. Older implementations must fail closed.
     */
    @PostMapping("/checkShareAuth/{uid}")
    default void checkShareAuth(@PathVariable("uid") Long uid, @RequestBody BusiPerCheckDTO checkDTO) {
        throw new UnsupportedOperationException("Share authorization unavailable");
    }

    @Operation(summary = "权限查询")
    @ApiOperationSupport(order = 9)
    @PostMapping("/queryAuth/{id}")
    PermissionValVO queryAuth(@PathVariable("id") Long id);

    @GetMapping("/query2Root/{id}/{flag}/{logOT}")
    List<ResourceNodeVO> query2Root(@PathVariable("id") Long id, @PathVariable("flag") Integer flag, Integer logOT);

    @GetMapping("/checkEmpty")
    boolean checkEmpty();

    @GetMapping("/OrgNameForResource")
    String OrgNameForResource(ExportTaskDTO exportTaskDTO);

    void editResourceExtraFlag(BusiResourceEditor editor);

    @PostMapping("/batchAuthorize")
    void batchAuthorize(@RequestBody BusiBatchAuthorizeRequest request);

    @Hidden
    @PostMapping("/revert")
    void revert();
}
