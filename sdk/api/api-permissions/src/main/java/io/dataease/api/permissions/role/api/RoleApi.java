package io.dataease.api.permissions.role.api;

import io.dataease.api.permissions.role.dto.*;
import io.dataease.api.permissions.role.vo.ExternalUserVO;
import io.dataease.api.permissions.role.vo.RoleDetailVO;
import io.dataease.api.permissions.role.vo.RoleVO;
import io.dataease.auth.DeApiPath;
import io.dataease.auth.DePermit;
import io.dataease.model.KeywordRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static io.dataease.constant.AuthResourceEnum.ROLE;

@DeApiPath(value = "/role", rt = ROLE)
public interface RoleApi {

    @DePermit("m:read")
    @PostMapping("/query")
    List<RoleVO> query(@RequestBody KeywordRequest request);

    @DePermit("m:read")
    @PostMapping("/create")
    void create(@RequestBody RoleCreator creator);

    @DePermit({"m:read", "#p0.id + ':manage'"})
    @PostMapping("/edit")
    void edit(@RequestBody RoleEditor editor);

    @DePermit({"m:read", "#p0.rid + ':manage'"})
    @PostMapping("/mountUser")
    void mountUser(@RequestBody MountUserRequest request);

    @DePermit({"m:read", "#p0.rid + ':manage'"})
    @PostMapping("/mountExternalUser")
    void mountExternalUser(@RequestBody MountExternalUserRequest request);

    @GetMapping("/searchExternalUser/{keyword}")
    ExternalUserVO searchExternalUser(@PathVariable("keyword") String keyword);

    @DePermit({"m:read", "#p0.rid + ':manage'"})
    @PostMapping("/unMountUser")
    void unMountUser(@RequestBody UnmountUserRequest request);

    @PostMapping("/user/option")
    List<RoleVO> optionForUser(@RequestBody RoleRequest request);

    @PostMapping("/user/selected")
    List<RoleVO> selectedForUser(@RequestBody RoleRequest request);

    @GetMapping("/detail/{rid}")
    RoleDetailVO detail(@PathVariable("rid") Long rid);

    @DePermit({"m:manage", "#p0 + ':manage'"})
    @PostMapping("/delete/{rid}")
    void delete(@PathVariable("rid") Long rid);

    @PostMapping("/beforeUnmountInfo")
    Integer beforeUnmountInfo(@RequestBody UnmountUserRequest request);

    @PostMapping("/copy")
    void copy(@RequestBody RoleCopyRequest request);

    @PostMapping("/byCurOrg")
    List<RoleVO> byCurOrg(@RequestBody KeywordRequest request);
}
