package io.dataease.api.permissions.org.api;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.permissions.org.dto.OrgCreator;
import io.dataease.api.permissions.org.dto.OrgDelRequest;
import io.dataease.api.permissions.org.dto.OrgEditor;
import io.dataease.api.permissions.org.dto.OrgLazyRequest;
import io.dataease.api.permissions.org.vo.LazyTreeVO;
import io.dataease.api.permissions.org.vo.MountedVO;
import io.dataease.api.permissions.org.vo.OrgDetailVO;
import io.dataease.api.permissions.org.vo.OrgPageVO;
import io.dataease.auth.DeApiPath;
import io.dataease.auth.DePermit;
import io.dataease.model.KeywordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static io.dataease.constant.AuthResourceEnum.ORG;

@Tag(name = "组织")
@ApiSupport(order = 886, author = "fit2cloud-someone")
@DeApiPath(value = "/org", rt = ORG)
public interface OrgApi {

    @Operation(summary = "查询组织树")
    @GetMapping("/page/tree")
    // @DePermit("m:read")
    List<OrgPageVO> pageTree();

    @Operation(summary = "懒加载组织树")
    @PostMapping("/page/lazyTree")
    @DePermit("m:read")
    LazyTreeVO lazyPageTree(@RequestBody OrgLazyRequest request);

    @Operation(summary = "创建")
    @DePermit({"m:read"})
    @PostMapping("/page/create")
    Long create(@RequestBody OrgCreator creator);

    @Operation(summary = "编辑")
    @DePermit({"m:read", "#p0.id+':manage'"})
    @PostMapping("/page/edit")
    void edit(@RequestBody OrgEditor editor);

    @Operation(summary = "删除")
    @PostMapping("/page/delete")
    @DePermit({"m:read", "#p0.id+':manage'"})
    void delete(@RequestBody OrgDelRequest request);

    @Operation(summary = "查询权限内组织树")
    @PostMapping("/mounted")
    List<MountedVO> mounted(@RequestBody KeywordRequest request);

    @Operation(summary = "当前用户日志数据范围(null=全局,空=普通用户,非空=组织范围)")
    @GetMapping("/logScope")
    List<Long> logScope();

    @Operation(hidden = true)
    @GetMapping("/detail/{oid}")
    OrgDetailVO detail(@PathVariable("oid") Long oid);

    @Operation(hidden = true)
    @GetMapping("/subOrgs")
    List<String> subOrgs();
}
