package io.dataease.login;

import io.dataease.api.login.api.LoginApi;
import io.dataease.api.login.dto.PwdLoginDTO;
import io.dataease.login.manage.LoginManage;
import io.dataease.utils.Md5Utils;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.utils.TokenUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class LoginServer implements LoginApi {

    @Resource
    private LoginManage loginManage;

    @Override
    public String localLogin(PwdLoginDTO dto) {
        TokenUserBO tokenUserBO = loginManage.localBuild(dto);
        String md5Pwd = Md5Utils.md5(dto.getPwd());
        return TokenUtils.generate(tokenUserBO, md5Pwd);
    }

    @Override
    public String oidcLogin(String code, String state) {
        // 从请求头获取userInfo 然后判断是否要新建用户 最后调用generate生成token
        return null;
    }
}
