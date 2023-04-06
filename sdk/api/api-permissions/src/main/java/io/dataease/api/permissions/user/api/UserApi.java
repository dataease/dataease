package io.dataease.api.permissions.user.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.dataease.api.permissions.role.dto.UserRequest;
import io.dataease.api.permissions.user.dto.UserCreator;
import io.dataease.api.permissions.user.dto.UserEditor;
import io.dataease.api.permissions.user.vo.UserGridVO;
import io.dataease.api.permissions.user.vo.UserItem;
import io.dataease.auth.DeApiPath;
import io.dataease.auth.DePermit;
import io.dataease.request.BaseGridRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


@DeApiPath("/user")
public interface UserApi {

    @DePermit("read")
    @PostMapping("/pager/{goPage}/{pageSize}")
    IPage<UserGridVO> pager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody BaseGridRequest request);

    @DePermit("#p0+':read'")
    @GetMapping("/queryById/{id}")
    UserGridVO queryById(@PathVariable("id") Long id);


    @DePermit("read")
    @PostMapping("/create")
    void create(@RequestBody UserCreator creator);

    @DePermit("read")
    @PostMapping("/edit")
    void edit(@RequestBody UserEditor editor);

    @DePermit("read")
    @PostMapping("/delete")
    void delete(@PathVariable("id") Long id);


    @PostMapping("/role/option")
    List<UserItem> optionForRole(@RequestBody UserRequest request);

    @PostMapping("/role/selected")
    List<UserItem> selectedForRole(@RequestBody UserRequest request);


}
