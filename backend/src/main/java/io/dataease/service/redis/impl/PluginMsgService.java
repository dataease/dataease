package io.dataease.service.redis.impl;

import io.dataease.service.redis.RedisMessageBroadcast;
import org.springframework.stereotype.Service;

@Service
public class PluginMsgService implements RedisMessageBroadcast {

    @Override
    public void messageCallBack(Object arg) {

    }
}
