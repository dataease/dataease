package io.dataease.api.permissions.user.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.dataease.api.permissions.role.dto.UserRequest;
import io.dataease.api.permissions.user.dto.UserCreator;
import io.dataease.api.permissions.user.dto.UserEditor;
import io.dataease.api.permissions.user.vo.CurUserVO;
import io.dataease.api.permissions.user.vo.UserFormVO;
import io.dataease.api.permissions.user.vo.UserGridVO;
import io.dataease.api.permissions.user.vo.UserItem;
import io.dataease.auth.DeApiPath;
import io.dataease.auth.DePermit;
import io.dataease.model.KeywordRequest;
import io.dataease.request.BaseGridRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


@DeApiPath(value = "/user", rt = "user")
public interface UserApi {

    @DePermit("m:read")
    @PostMapping("/pager/{goPage}/{pageSize}")
    IPage<UserGridVO> pager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody BaseGridRequest request);

    @DePermit({"m:read", "#p0 + ':read'"})
    @GetMapping("/queryById/{id}")
    UserFormVO queryById(@PathVariable("id") Long id);


    @DePermit("m:read")
    @PostMapping("/create")
    void create(@RequestBody UserCreator creator);

    @DePermit({"m:read", "#p0.id + ':read'"})
    @PostMapping("/edit")
    void edit(@RequestBody UserEditor editor);

    @DePermit({"m:read", "#p0 + ':read'"})
    @PostMapping("/delete/{id}")
    void delete(@PathVariable("id") Long id);


    @PostMapping("/role/option")
    List<UserItem> optionForRole(@RequestBody UserRequest request);

    @PostMapping("/role/selected")
    List<UserItem> selectedForRole(@RequestBody UserRequest request);

    @PostMapping("/switch/{oId}")
    String switchOrg(@PathVariable("oId") Long oId);

    @GetMapping("/info")
    CurUserVO info();

    @PostMapping("/byCurOrg")
    List<UserItem> byCurOrg(@RequestBody KeywordRequest request);
}
