package io.dataease.service.redis.impl;

import com.google.gson.Gson;
import io.dataease.service.redis.RedisMessageBroadcast;
import io.dataease.websocket.entity.WsMessage;
import io.dataease.websocket.service.impl.StandaloneWsService;
import io.dataease.websocket.util.WsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WsMsgService implements RedisMessageBroadcast<Map> {

    private static Gson json = new Gson();

    @Autowired
    private StandaloneWsService standaloneWsService;

    @Override
    public void messageCallBack(Map arg) {
        WsMessage message = json.fromJson(json.toJson(arg), WsMessage.class);
        Long userId = message.getUserId();
        if (WsUtil.isOnLine(userId)) {
            standaloneWsService.releaseMessage(message);
        }
    }
}
