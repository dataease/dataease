package io.dataease.utils;

import io.dataease.auth.bo.TokenUserBO;

public class UserUtils {

    public static void setUserInfo(TokenUserBO userBO) {
        AuthUtils.setUser(userBO);
    }

    public static void setDesktopUser() {
        TokenUserBO bo = new TokenUserBO();
        bo.setUserId(1L);
        bo.setDefaultOid(1L);
        AuthUtils.setUser(bo);
    }
}
