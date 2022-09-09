package io.dataease.service.message.service.strategy;

import io.dataease.auth.entity.SysUserEntity;
import io.dataease.auth.service.AuthUserService;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.wecom.service.WecomXpackService;
import io.dataease.service.message.service.SendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("sendWecom")
public class SendWecom implements SendService {

    @Autowired
    private AuthUserService authUserService;
    @Override
    public void sendMsg(Long userId, Long typeId, String content, String param) {
        SysUserEntity userEntity = authUserService.getUserById(userId);

        if (userEntity.getFrom() == 4 && authUserService.supportWecom()) {
            String username = userEntity.getUsername();
            WecomXpackService wecomXpackService = SpringContextUtil.getBean(WecomXpackService.class);
            List<String> userIds = new ArrayList<>();
            userIds.add(username);
            wecomXpackService.pushMsg(userIds, content);
        }
    }
}
