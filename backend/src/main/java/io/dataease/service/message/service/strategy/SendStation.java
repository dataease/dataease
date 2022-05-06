package io.dataease.service.message.service.strategy;

import io.dataease.plugins.common.base.domain.SysMsg;
import io.dataease.plugins.common.base.mapper.SysMsgMapper;
import io.dataease.service.message.service.SendService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("sendStation")
public class SendStation implements SendService {

    @Resource
    private SysMsgMapper sysMsgMapper;

    @Override
    public void sendMsg(Long userId, Long typeId, String content, String param) {

        SysMsg sysMsg = new SysMsg();
        sysMsg.setUserId(userId);
        sysMsg.setTypeId(typeId);
        sysMsg.setContent(content);
        sysMsg.setStatus(false);
        sysMsg.setCreateTime(System.currentTimeMillis());
        sysMsg.setParam(param);

        sysMsgMapper.insert(sysMsg);

    }
}
