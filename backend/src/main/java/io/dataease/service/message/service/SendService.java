package io.dataease.service.message.service;

public interface SendService {


    void sendMsg(Long userId, Long typeId, String content, String param);
}
