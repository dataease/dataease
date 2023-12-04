package io.dataease.utils;

import io.dataease.auth.bo.TokenUserBO;
import org.apache.commons.lang3.ObjectUtils;

public class AuthUtils {

    private static final Long SYS_ADMIN_UID = 1L;

    private static final ThreadLocal<TokenUserBO> USER_INFO = new ThreadLocal<TokenUserBO>();

    public static TokenUserBO getUser() {
        if (ObjectUtils.isNotEmpty(USER_INFO.get()))
            return USER_INFO.get();
        return null;
    }

    public static void setUser(TokenUserBO userBO) {
        USER_INFO.set(userBO);
    }

    public static boolean isSysAdmin() {
        TokenUserBO user = null;
        if (ObjectUtils.isEmpty(user = getUser())) {
            return false;
        }
        Long userId = user.getUserId();
        return isSysAdmin(userId);
    }

    public static boolean isSysAdmin(Long userId) {
        return userId.equals(SYS_ADMIN_UID);
    }


}
