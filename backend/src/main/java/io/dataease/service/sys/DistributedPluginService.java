package io.dataease.service.sys;

import io.dataease.commons.condition.RedisStatusCondition;
import io.dataease.commons.constants.RedisConstants;
import io.dataease.commons.model.RedisMessage;
import io.dataease.plugins.entity.PluginOperate;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Conditional({RedisStatusCondition.class})
public class DistributedPluginService {

    @Resource
    private RedisTemplate redisTemplate;


    public void pushBroadcast(PluginOperate operate) {
        if (ObjectUtils.isEmpty(operate) || ObjectUtils.isEmpty(operate.getPlugin()) || StringUtils.isBlank(operate.getSenderIp()))
            return;

        RedisMessage<PluginOperate> msg = new RedisMessage();
        msg.setType(RedisConstants.PLUGIN_INSTALL_MSG);
        msg.setData(operate);
        redisTemplate.convertAndSend(RedisConstants.GLOBAL_REDIS_TOPIC, msg);
    }
}
