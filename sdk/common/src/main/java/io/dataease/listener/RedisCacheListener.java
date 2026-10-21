package io.dataease.listener;

import io.dataease.utils.LogUtil;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@ConditionalOnExpression("'${spring.cache.type}'.equals('redis')")
@Component
@Order(100)
public class RedisCacheListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final int DELETE_BATCH_SIZE = 1000;

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            deleteKeysContainingString(redisTemplate, "de_v3_");
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
    }


    public void deleteKeysContainingString(RedisTemplate<String, String> redisTemplate, String searchString) {
        // 扫描所有的key
        ScanOptions scanOptions = ScanOptions.scanOptions().match("*" + searchString + "*").count(1000).build();
        try (RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
             Cursor<byte[]> cursor = connection.scan(scanOptions)) {
            List<byte[]> keysToDelete = new ArrayList<>(DELETE_BATCH_SIZE);
            while (cursor.hasNext()) {
                // SCAN 返回原始键，避免经 RedisTemplate 再次序列化。
                keysToDelete.add(cursor.next());
                if (keysToDelete.size() == DELETE_BATCH_SIZE) {
                    connection.keyCommands().del(keysToDelete.toArray(new byte[0][]));
                    keysToDelete.clear();
                }
            }
            if (!keysToDelete.isEmpty()) {
                connection.keyCommands().del(keysToDelete.toArray(new byte[0][]));
            }
        }
    }
}
