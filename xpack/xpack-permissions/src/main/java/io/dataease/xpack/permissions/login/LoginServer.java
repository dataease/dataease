package io.dataease.xpack.permissions.login;


import io.dataease.api.permissions.login.api.LoginApi;
import io.dataease.api.permissions.login.dto.PwdLoginDTO;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.utils.*;
import io.dataease.xpack.permissions.login.manage.LoginManage;
import io.dataease.xpack.permissions.utils.PerTokenUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class LoginServer implements LoginApi {

    @Resource
    private LoginManage loginManage;

    @Override
    public String localLogin(PwdLoginDTO dto) {
        TokenUserBO tokenUserBO = loginManage.localBuild(dto);
        String pwd = dto.getPwd();
        String md5Pwd = Md5Utils.md5(RsaUtils.decryptStr(pwd));
        return PerTokenUtils.generate(tokenUserBO, md5Pwd);
    }

    @Override
    public String platformLogin(Integer origin) {
        if (origin == 2) {
            String xUserinfo = ServletUtils.getXUserinfo();
            LogUtil.info("oidc user [" + xUserinfo + "] login");
        } else if (origin == 3) {
            String casUser = ServletUtils.getCasUser();
            LogUtil.info("cas user [" + casUser + "] login");
        }
        TokenUserBO tokenUserBO = new TokenUserBO();
        tokenUserBO.setUserId(1L);
        tokenUserBO.setDefaultOid(1L);
        String md5Pwd = "83d923c9f1d8fcaa46cae0ed2aaa81b5";
        return PerTokenUtils.generate(tokenUserBO, md5Pwd);
    }

    @Override
    public void logout() {
        AuthUtils.setUser(null);
        //remember destroy the token
    }
}
