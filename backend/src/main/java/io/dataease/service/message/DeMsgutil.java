package io.dataease.service.message;

import io.dataease.base.domain.SysMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class DeMsgutil {

    private static Map<Integer, String> routerMap ;

    @PostConstruct
    public void  init() {
        routerMap = new HashMap<>();
        routerMap.put(0, "panel");
        routerMap.put(1, "dataset");
    }

    private static SysMsgService sysMsgService;

    @Autowired
    public void setSysMsgService(SysMsgService sysMsgService) {
        DeMsgutil.sysMsgService = sysMsgService;
    }

    public static void sendMsg(Long userId, int type, String content) {
        SysMsg sysMsg = new SysMsg();
        sysMsg.setUserId(userId);
        sysMsg.setType(type);
        sysMsg.setContent(content);
        sysMsg.setRouter(routerMap.get(type));
        sysMsg.setStatus(false);
        sysMsg.setCreateTime(System.currentTimeMillis());
        sysMsgService.save(sysMsg);
    }

    public static void sendMsg(Long userId, int type, String content, String param) {
        SysMsg sysMsg = new SysMsg();
        sysMsg.setUserId(userId);
        sysMsg.setType(type);
        sysMsg.setContent(content);
        sysMsg.setRouter(routerMap.get(type));
        sysMsg.setStatus(false);
        sysMsg.setCreateTime(System.currentTimeMillis());
        sysMsg.setParam(param);
        sysMsgService.save(sysMsg);
    }

}
