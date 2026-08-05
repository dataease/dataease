package io.dataease.api.permissions.auth.api;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.permissions.auth.dto.ResourcePermissionRequest;
import io.dataease.api.permissions.auth.dto.SubjectByResourceRequest;
import io.dataease.api.permissions.auth.vo.ResourcePermissionVO;
import io.dataease.api.permissions.auth.vo.SubjectVO;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "资源权限")
@ApiSupport(order = 885, author = "fit2cloud-someone")
public interface ResourceAuthApi {

    @Hidden
    List<ResourcePermissionVO> queryResourcePermission(ResourcePermissionRequest request);

    @Operation(summary = "查询资源已授权用户ID")
    @ApiOperationSupport(order = 1)
    @PostMapping("/queryAuthorizedUserIds")
    List<Long> queryAuthorizedUserIds(@RequestBody SubjectByResourceRequest request);

    @Operation(summary = "查询全部用户或角色")
    @ApiOperationSupport(order = 2)
    @GetMapping("/queryAllSubjects/{type}")
    List<SubjectVO> queryAllSubjects(@PathVariable("type") Integer type);
}
