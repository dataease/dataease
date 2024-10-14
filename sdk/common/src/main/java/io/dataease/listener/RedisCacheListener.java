package io.dataease.listener;

import io.dataease.utils.LogUtil;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@ConditionalOnExpression("'${spring.cache.type}'.equals('redis')")
@Component
@Order(100)
public class RedisCacheListener implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            deleteKeysContainingString(redisTemplate, "de_v2_");
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
    }


    public void deleteKeysContainingString(RedisTemplate<String, String> redisTemplate, String searchString) {
        // 扫描所有的key
        ScanOptions scanOptions = ScanOptions.scanOptions().match("*" + searchString + "*").count(1000).build();
        Cursor<byte[]> cursor = redisTemplate.getConnectionFactory()
                .getConnection()
                .scan(scanOptions);

        List<byte[]> keysToDelete = new ArrayList<>();
        while (cursor.hasNext()) {
            keysToDelete.add(cursor.next());
        }

        if (!keysToDelete.isEmpty()) {
            List<String> keys = new ArrayList<>(keysToDelete.size());
            for (byte[] key : keysToDelete) {
                keys.add(new String(key, StandardCharsets.UTF_8));
            }
            redisTemplate.delete(keys);
        }
    }
}
