package io.dataease.api.permissions.role.api;

import io.dataease.api.permissions.role.dto.MountUserRequest;
import io.dataease.api.permissions.role.dto.RoleRequest;
import io.dataease.api.permissions.role.dto.UnmountUserRequest;
import io.dataease.api.permissions.role.vo.RoleCreator;
import io.dataease.api.permissions.role.vo.RoleDetailVO;
import io.dataease.api.permissions.role.vo.RoleEditor;
import io.dataease.api.permissions.role.vo.RoleVO;
import io.dataease.auth.DeApiPath;
import io.dataease.model.KeywordRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@DeApiPath("/role")
public interface RoleApi {

    @PostMapping("/query")
    List<RoleVO> query(@RequestBody KeywordRequest request);

    @PostMapping("/create")
    void create(@RequestBody RoleCreator creator);

    @PostMapping("/edit")
    void edit(@RequestBody RoleEditor editor);

    @PostMapping("/mountUser")
    void mountUser(@RequestBody MountUserRequest request);

    @PostMapping("/mountExternalUser/{uId}")
    void mountExternalUser(@PathVariable("uId") Long uId);

    @PostMapping("/unMountUser")
    void unMountUser(@RequestBody UnmountUserRequest request);

    @PostMapping("/user/option")
    List<RoleVO> optionForUser(@RequestBody RoleRequest request);

    @PostMapping("/user/selected")
    List<RoleVO> selectedForUser(@RequestBody RoleRequest request);

    @GetMapping("/detail/{rid}")
    RoleDetailVO detail(@PathVariable("rid") Long rid);

    @PostMapping("/delete/{rid}")
    void delete(@PathVariable("rid") Long rid);

    @PostMapping("/beforeUnmountInfo")
    Integer beforeUnmountInfo(@RequestBody UnmountUserRequest request);
}
