package io.dataease.websocket.service;

import io.dataease.websocket.entity.WsMessage;


public interface WsService {

    void releaseMessage(WsMessage wsMessage);


}
