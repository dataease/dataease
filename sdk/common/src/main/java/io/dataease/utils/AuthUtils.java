package io.dataease.utils;

import io.dataease.auth.bo.TokenUserBO;
import org.apache.commons.lang3.ObjectUtils;

public class AuthUtils {

    private static final ThreadLocal<TokenUserBO> USER_INFO = new ThreadLocal<TokenUserBO>();

    public static TokenUserBO getUser() {
        if (ObjectUtils.isNotEmpty(USER_INFO.get()))
            return USER_INFO.get();
        return null;
    }

    public static void setUser(TokenUserBO userBO) {
        USER_INFO.set(userBO);
    }
}
