package io.dataease.xpack.permissions.user.server;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.dataease.api.permissions.role.dto.UserRequest;
import io.dataease.api.permissions.user.api.UserApi;
import io.dataease.api.permissions.user.dto.UserCreator;
import io.dataease.api.permissions.user.dto.UserEditor;
import io.dataease.api.permissions.user.vo.CurUserVO;
import io.dataease.api.permissions.user.vo.UserFormVO;
import io.dataease.api.permissions.user.vo.UserGridVO;
import io.dataease.api.permissions.user.vo.UserItem;
import io.dataease.model.KeywordRequest;
import io.dataease.request.BaseGridRequest;
import io.dataease.utils.AuthUtils;
import io.dataease.xpack.permissions.user.manage.UserPageManage;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserServer implements UserApi {


    @Resource
    private UserPageManage userPageManage;

    @Override
    public IPage<UserGridVO> pager(int goPage, int pageSize, BaseGridRequest request) {
        Page<UserGridVO> page = new Page(goPage, pageSize);
        return userPageManage.pager(page, request);
    }

    @Override
    public UserFormVO queryById(Long id) {
        return userPageManage.queryForm(id);
    }

    @Override
    public void create(UserCreator creator) {
        userPageManage.save(creator);
    }

    @Override
    public void edit(UserEditor editor) {
        userPageManage.edit(editor);
    }

    @Override
    public void delete(Long id) {
        userPageManage.delete(id);
    }

    @Override
    public List<UserItem> optionForRole(UserRequest request) {
        return userPageManage.optionForRole(request);
    }

    @Override
    public List<UserItem> selectedForRole(UserRequest request) {
        return userPageManage.selectedForRole(request);
    }

    @Override
    @Transactional
    public String switchOrg(Long oId) {
        Long userId = AuthUtils.getUser().getUserId();
        userPageManage.switchOrg(userId, oId);
        return userPageManage.switchToken();
    }

    @Override
    public CurUserVO info() {
        return userPageManage.getUserInfo();
    }

    @Override
    public List<UserItem> byCurOrg(KeywordRequest request) {
        Long oid = AuthUtils.getUser().getDefaultOid();
        return userPageManage.queryForOrg(request.getKeyword(), oid);
    }
}
