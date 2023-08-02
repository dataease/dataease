package io.dataease.xpack.permissions.user.server;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.dataease.api.permissions.role.dto.UserRequest;
import io.dataease.api.permissions.user.api.UserApi;
import io.dataease.api.permissions.user.dto.LangSwitchRequest;
import io.dataease.api.permissions.user.dto.UserCreator;
import io.dataease.api.permissions.user.dto.UserEditor;
import io.dataease.api.permissions.user.vo.*;
import io.dataease.exception.DEException;
import io.dataease.i18n.Lang;
import io.dataease.license.config.XpackResource;
import io.dataease.model.KeywordRequest;
import io.dataease.request.BaseGridRequest;
import io.dataease.utils.AuthUtils;
import io.dataease.xpack.permissions.user.manage.UserBatchImportManage;
import io.dataease.xpack.permissions.user.manage.UserPageManage;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/user")
@Primary
@XpackResource(excludes = {"userCount", "info"})
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
    public void batchDel(List<Long> ids) {
        userPageManage.batchDel(ids);
    }

    @Override
    public List<UserItemVO> optionForRole(UserRequest request) {
        List<UserItemVO> userItems = userPageManage.optionForRole(request);
        if (CollectionUtil.isEmpty(userItems)) return userItems;
        return userItems.stream().filter(user -> !AuthUtils.isSysAdmin(user.getId())).toList();
    }

    @Override
    public IPage<UserItemVO> selectedForRole(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody UserRequest request) {
        Page<UserItemVO> page = new Page(goPage, pageSize);
        IPage<UserItemVO> iPage = userPageManage.selectedWithRole(page, request);
        return iPage;
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

    @Override
    public int userCount() {
        return userPageManage.userCount();
    }

    @Override
    public void switchLanguage(LangSwitchRequest request) {
        String lang = request.getLang();
        if (StringUtils.equalsIgnoreCase(Lang.zh_CN.getDesc(), lang)) {
            lang = Lang.zh_CN.getDesc();
        } else if (StringUtils.equalsAnyIgnoreCase(lang, "en", "tw")) {
            lang = lang.toLowerCase();
        } else {
            DEException.throwException("无效language");
        }
        userPageManage.switchLang(AuthUtils.getUser().getUserId(), lang);
    }

    @Resource
    private UserBatchImportManage userBatchImportManage;

    @Override
    public void excelTemplate() {
        userBatchImportManage.templateDown();
    }

    @Override
    public UserImportVO batchImport(MultipartFile file) {
        return userBatchImportManage.upload(file);
    }

    @Override
    public void errorRecord(String key) {
        userBatchImportManage.exportErrorExcel(key);
    }

    @Override
    public void clearErrorRecord(String key) {
        userBatchImportManage.clearErrorData(key);
    }

    @Override
    public String defaultPwd() {
        return userPageManage.defaultPwd();
    }

    @Override
    public void resetPwd(Long id) {
        userPageManage.resetPwd(id);
    }
}
