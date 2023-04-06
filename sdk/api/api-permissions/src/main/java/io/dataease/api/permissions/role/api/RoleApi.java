package io.dataease.api.permissions.role.api;

import io.dataease.api.permissions.role.vo.RoleCreator;
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

    void mountUser(@RequestBody List<Long> uIds);

    @PostMapping("/mountExternalUser/{uId}")
    void mountExternalUser(@PathVariable("uId") Long uId);

    @PostMapping("/unMountUser/{uId}")
    void unMountUser(@PathVariable("uId") Long uId);

    @GetMapping("/user/option/{uId}")
    List<RoleVO> optionForUser(@PathVariable("uId") Long uId);

    @GetMapping("/user/selected/{uId}")
    List<RoleVO> selectedForUser(@PathVariable("uId") Long uId);
}
