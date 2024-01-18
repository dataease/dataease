package io.dataease.utils;


import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;

public class IDUtils {

    private static SnowFlake snowFlake = new SnowFlake(1, 1);

    public static String randomID(Integer num) {
        num = ObjectUtils.isEmpty(num) ? 16 : num;
        return RandomStringUtils.randomAlphanumeric(num);
    }

    // 主键请不要使用字符串 推荐雪花算法
    public static Long snowID() {
        return snowFlake.nextId();
    }
}
