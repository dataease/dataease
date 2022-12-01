package io.dataease.auth.service.impl;

import io.dataease.auth.service.DeLimitService;
import io.dataease.commons.condition.RedisStatusCondition;
import io.dataease.commons.utils.LogUtil;
import org.slf4j.Logger;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Conditional({RedisStatusCondition.class})
@Component
@Primary
public class RedisLimitServiceImpl implements DeLimitService {

    Logger log = LogUtil.getLogger();
    private final static String REDIS_LIMIT_KEY_PREFIX = "limit:";
    @Resource
    private RedisScript<Long> limitRedisScript;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Boolean checkRestricted(String key, long max, long timeout, TimeUnit timeUnit) {
        key = REDIS_LIMIT_KEY_PREFIX + key;
        long ttl = timeUnit.toMillis(timeout);
        long now = Instant.now().toEpochMilli();
        long expired = now - ttl;

        Long executeTimes = stringRedisTemplate.execute(limitRedisScript, Collections.singletonList(key), now + "", ttl + "", expired + "", max + "");
        if (executeTimes != null) {
            if (executeTimes == 0) {

                log.error("【{}】在单位时间 {} 毫秒内已达到访问上限，当前接口上限 {}", key, ttl, max);
                return true;
            } else {
                log.info("【{}】在单位时间 {} 毫秒内访问 {} 次", key, ttl, executeTimes);
                return false;
            }
        }
        return false;
    }
}
