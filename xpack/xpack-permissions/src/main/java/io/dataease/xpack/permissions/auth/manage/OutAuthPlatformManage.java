package io.dataease.xpack.permissions.auth.manage;

import io.dataease.api.permissions.auth.dto.OutAuthPlatformLoginRequest;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.utils.TokenUtils;
import io.dataease.xpack.permissions.login.manage.LoginManage;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class OutAuthPlatformManage {

    private static final String defaultEmail = "@de2.com";

    @Resource
    private LoginManage loginManage;

    public String login(OutAuthPlatformLoginRequest request) {
        String account = request.getAccount();
        String email = null;
        if (StringUtils.isBlank(email = request.getEmail())) {
            email = account + defaultEmail;
        }
        TokenUserBO tokenUserBO = new TokenUserBO();
        tokenUserBO.setUserId(1L);
        tokenUserBO.setDefaultOid(1L);
        return TokenUtils.generate(tokenUserBO, "83d923c9f1d8fcaa46cae0ed2aaa81b5");
    }
}
