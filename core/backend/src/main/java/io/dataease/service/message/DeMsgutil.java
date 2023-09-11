package io.dataease.service.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class DeMsgutil {

    private static SysMsgService sysMsgService;

    @Autowired
    public void setSysMsgService(SysMsgService sysMsgService) {
        DeMsgutil.sysMsgService = sysMsgService;
    }

    public static void sendMsg(Long userId, Long typeId, String content, String param) {

        sysMsgService.sendMsg(userId, typeId, content, param);
    }


}
