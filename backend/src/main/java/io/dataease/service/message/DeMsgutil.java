package io.dataease.service.message;

import io.dataease.base.domain.SysMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class DeMsgutil {



    private static SysMsgService sysMsgService;

    @Autowired
    public void setSysMsgService(SysMsgService sysMsgService) {
        DeMsgutil.sysMsgService = sysMsgService;
    }



    public static void sendMsg(Long userId, Long typeId, Long channelId, String content, String param) {
        SysMsg sysMsg = new SysMsg();
        sysMsg.setUserId(userId);
        sysMsg.setTypeId(typeId);
        sysMsg.setContent(content);
        sysMsg.setStatus(false);
        sysMsg.setCreateTime(System.currentTimeMillis());
        sysMsg.setParam(param);
        sysMsgService.save(sysMsg);
    }


}
