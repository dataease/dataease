package io.dataease.websocket.aop;

import io.dataease.websocket.entity.WsMessage;
import io.dataease.websocket.service.WsService;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Aspect
@Component
public class WSTrigger {

    @Autowired
    private WsService wsService;

    @AfterReturning(value = "execution(* io.dataease.service.message.service.strategy.SendStation.sendMsg(..))")
    public void after(JoinPoint point) {
        Object[] args = point.getArgs();
        Optional.ofNullable(args).ifPresent(objs -> {
            if (ArrayUtils.isEmpty(objs)) return;
            Object arg = args[0];
            Long userId = (Long) arg;
            WsMessage message = new WsMessage(userId, "/web-msg-topic", "refresh");
            wsService.releaseMessage(message);
        });

    }
}
