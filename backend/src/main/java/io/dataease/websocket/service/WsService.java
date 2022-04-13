package io.dataease.websocket.service;

import io.dataease.websocket.entity.WsMessage;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Component
public class WsService {



    @Resource
    private SimpMessagingTemplate messagingTemplate;




    public void releaseMessage (List<WsMessage> wsMessages) {
        Optional.ofNullable(wsMessages).ifPresent(messages -> {
            messages.forEach(this::releaseMessage);
        });
    }

    public void releaseMessage(WsMessage wsMessage){
        if(ObjectUtils.isEmpty(wsMessage) || ObjectUtils.isEmpty(wsMessage.getUserId()) ||  ObjectUtils.isEmpty(wsMessage.getTopic())) return;


        messagingTemplate.convertAndSendToUser(String.valueOf(wsMessage.getUserId()), wsMessage.getTopic(),wsMessage.getData());


    }


}
