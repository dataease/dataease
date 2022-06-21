package io.dataease.websocket.service.impl;

import io.dataease.commons.condition.RedisStatusCondition;
import io.dataease.commons.constants.RedisConstants;
import io.dataease.commons.model.RedisMessage;
import io.dataease.websocket.entity.WsMessage;
import io.dataease.websocket.service.WsService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Conditional({RedisStatusCondition.class})
@Primary
public class DistributedWsService implements WsService {

    @Autowired
    private RedisTemplate redisTemplate;


    public void releaseMessage(WsMessage wsMessage){
        if(ObjectUtils.isEmpty(wsMessage) || ObjectUtils.isEmpty(wsMessage.getUserId()) ||  ObjectUtils.isEmpty(wsMessage.getTopic())) return;

        RedisMessage<WsMessage> msg = new RedisMessage();
        msg.setType(RedisConstants.WEBSOCKET_MSG);
        msg.setData(wsMessage);
        redisTemplate.convertAndSend(RedisConstants.GLOBAL_REDIS_TOPIC, msg);
    }


}
