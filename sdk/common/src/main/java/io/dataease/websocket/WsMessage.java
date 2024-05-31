package io.dataease.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WsMessage<T> implements Serializable {
    private Long userId;

    private String topic;

    private T data;


}

