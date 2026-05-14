package io.dataease.websocket.util;


import io.dataease.permission.util.V3UserUtil;
import org.apache.commons.lang3.ObjectUtils;

import java.util.concurrent.CopyOnWriteArraySet;

public class WsUtil {

    private static final CopyOnWriteArraySet<Long> ONLINE_USERS = new CopyOnWriteArraySet();

    public static boolean onLine() {
        Long uid = V3UserUtil.getUid();
        if (ObjectUtils.isNotEmpty(uid))
            return onLine(uid);
        return false;
    }

    public static boolean onLine(Long userId) {
        return ONLINE_USERS.add(userId);
    }

    public static boolean offLine() {
        Long uid = V3UserUtil.getUid();
        if (ObjectUtils.isNotEmpty(uid))
            return offLine(uid);
        return false;
    }

    public static boolean offLine(Long userId) {
        return ONLINE_USERS.remove(userId);
    }

    public static boolean isOnLine(Long userId) {
        return ONLINE_USERS.contains(userId);
    }


}
