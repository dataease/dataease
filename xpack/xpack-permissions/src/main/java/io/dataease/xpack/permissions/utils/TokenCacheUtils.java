package io.dataease.xpack.permissions.utils;

import io.dataease.utils.CacheUtils;
import io.dataease.utils.DelayQueueUtils;

import java.util.concurrent.TimeUnit;

public class TokenCacheUtils {


    public static void add(Long uid, String token) {
        Long expireTime = PerTokenUtils.getExpireTime(TimeUnit.SECONDS);
        CacheUtils.put(PerConstant.USER_TOKEN_CACHE, uid.toString() + token, 1, expireTime, TimeUnit.SECONDS);
    }

    public static void delayDel(Long uid, String token) {
        String key = uid + token;
        DelayQueueUtils.execute(key, () -> del(uid, token), 5L);
    }

    public static void del(Long uid, String token) {
        CacheUtils.keyRemove(PerConstant.USER_TOKEN_CACHE, uid.toString() + token);
    }

    public static boolean tokenValid(Long uid, String token) {
        return CacheUtils.keyExist(PerConstant.USER_TOKEN_CACHE, uid.toString() + token);
    }

}
