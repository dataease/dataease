package io.dataease.service.redis;

public interface RedisMessageBroadcast<T> {

    void messageCallBack(T arg);
}
