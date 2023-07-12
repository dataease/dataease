package io.dataease.xpack.permissions.utils;

import io.dataease.utils.CacheUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TokenCacheUtils {

    private static List<String> delayQueueList = new ArrayList<>();

    private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public static void add(Long uid, String token) {
        Long expireTime = PerTokenUtils.getExpireTime(TimeUnit.SECONDS);
        CacheUtils.put(PerConstant.USER_TOKEN_CACHE, uid.toString() + token, 1, expireTime, TimeUnit.SECONDS);
    }

    public static void delayDel(Long uid, String token) {
        String key = uid + token;
        if (delayQueueList.contains(key)) return;
        delayQueueList.add(key);
        executorService.schedule(() -> {
            del(uid, token);
            delayQueueList.remove(key);
        }, 5L, TimeUnit.SECONDS);
    }

    public static void del(Long uid, String token) {
        CacheUtils.keyRemove(PerConstant.USER_TOKEN_CACHE, uid.toString() + token);
    }

    public static boolean tokenValid(Long uid, String token) {
        return CacheUtils.keyExist(PerConstant.USER_TOKEN_CACHE, uid.toString() + token);
    }

}
