package io.dataease.websocket.service.impl;

import io.dataease.websocket.WsMessage;
import io.dataease.websocket.WsService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


@Service
public class StandaloneWsService implements WsService {

    @Resource
    private SimpMessagingTemplate messagingTemplate;

    public void releaseMessage(WsMessage wsMessage){
        if(ObjectUtils.isEmpty(wsMessage) || ObjectUtils.isEmpty(wsMessage.getUserId()) ||  ObjectUtils.isEmpty(wsMessage.getTopic())) return;
        messagingTemplate.convertAndSendToUser(String.valueOf(wsMessage.getUserId()), wsMessage.getTopic(),wsMessage.getData());
    }
}
