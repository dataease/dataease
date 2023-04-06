package io.dataease.utils;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import org.apache.commons.lang3.ObjectUtils;

public class IDUtils {

    public static String randomID(Integer num) {
        num = ObjectUtils.isEmpty(num) ? 16 : num;
        return RandomUtil.randomString(16);
    }

    // 主键请不要使用字符串 推荐雪花算法
    public static Long snowID() {
        return IdUtil.getSnowflakeNextId();
    }
}
