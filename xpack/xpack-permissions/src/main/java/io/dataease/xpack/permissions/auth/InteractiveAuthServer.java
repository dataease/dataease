package io.dataease.xpack.permissions.auth;

import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.api.permissions.auth.dto.BusiResourceCreator;
import io.dataease.api.permissions.auth.dto.BusiResourceEditor;
import io.dataease.api.permissions.auth.dto.OutAuthPlatformLoginRequest;
import io.dataease.api.permissions.auth.vo.BusiPerVO;
import io.dataease.xpack.permissions.auth.manage.InteractiveAuthManage;
import io.dataease.xpack.permissions.auth.manage.OutAuthPlatformManage;
import io.dataease.xpack.permissions.auth.manage.SyncAuthManage;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/interactive")
@Primary
public class InteractiveAuthServer implements InteractiveAuthApi {

    @Resource
    private InteractiveAuthManage interactiveAuthManage;

    @Resource
    private SyncAuthManage syncAuthManage;

    @Resource
    private OutAuthPlatformManage outAuthPlatformManage;

    @Override
    public List<Long> menuIds() {
        return interactiveAuthManage.menuIds();
    }


    @Override
    public List<BusiPerVO> resource(String flag) {
        return interactiveAuthManage.resource(flag);
    }

    @Override
    public void saveResource(BusiResourceCreator creator) {
        syncAuthManage.syncResource(creator);
    }

    @Override
    public void editResource(BusiResourceEditor editor) {
        syncAuthManage.editResourceName(editor);
    }

    @Override
    public void delResource(Long id) {
        syncAuthManage.delResource(id);
    }

    @Override
    public boolean checkDel(Long id) {
        return syncAuthManage.checkDel(id);
    }

    @Override
    public String outAuthPlatformLogin(OutAuthPlatformLoginRequest request) {
        return outAuthPlatformManage.login(request);
    }
}
