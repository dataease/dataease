package io.dataease.xpack.permissions;

import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.xpack.permissions.auth.manage.InteractiveAuthManage;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/interactive/auth/")
@Primary
public class InteractiveAuthServer implements InteractiveAuthApi {

    @Resource
    private InteractiveAuthManage interactiveAuthManage;

    @Override
    public List<Long> menuIds() {
        return interactiveAuthManage.menuIds();
    }
}
