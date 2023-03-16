package io.dataease.api.permissions.user.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.dataease.api.permissions.user.vo.UserGridVO;
import io.dataease.auth.DeApiPath;
import io.dataease.auth.DePermit;
import io.dataease.request.BaseGridRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@DeApiPath("/user")
public interface UserApi {

    @DePermit("read")
    @PostMapping("/pager/{goPage}/{pageSize}")
    IPage<UserGridVO> pager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody BaseGridRequest request);

    @DePermit("#{id}:read")
    @GetMapping("/queryById/{id}")
    UserGridVO queryById(@PathVariable("id") Long id);

    @DePermit("#{vo.id}:modify")
    @PostMapping("/delete")
    void delete(@RequestBody UserGridVO vo);
}
