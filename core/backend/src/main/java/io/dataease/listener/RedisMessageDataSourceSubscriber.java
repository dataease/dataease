package io.dataease.listener;

import com.google.gson.Gson;
import io.dataease.commons.condition.RedisStatusCondition;
import io.dataease.commons.constants.RedisConstants;
import io.dataease.commons.utils.LogUtil;
import io.dataease.service.datasource.DatasourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;


@Conditional({RedisStatusCondition.class})
@Service
public class RedisMessageDataSourceSubscriber implements MessageListener {

    @Resource
    private RedisMessageListenerContainer redisMessageListenerContainer;

    private static final Gson json = new Gson();

    @Autowired
    private RedisTemplate redisTemplate;
    @Resource
    private DatasourceService datasourceService;

    /**
     * 启动之后订阅 topic
     */
    @EventListener
    public void init(ApplicationReadyEvent event) {
        String topic = RedisConstants.DS_REDIS_TOPIC;
        LogUtil.info("Subscribe Topic: " + topic);
        redisMessageListenerContainer.addMessageListener(new MessageListenerAdapter(this), new ChannelTopic(topic));
    }

    /**
     * @param message 消息内容
     * @param pattern 暂时用不到
     */
    public void onMessage(final Message message, final byte[] pattern) {
        try {

            byte[] messageBody = message.getBody();
            // 使用值序列化器转换
            Object o = redisTemplate.getValueSerializer().deserialize(messageBody);
            datasourceService.handleConnectionPool(o.toString(), "edit");
        }catch (Exception e){
            LogUtil.error(e);
        }
    }
}
