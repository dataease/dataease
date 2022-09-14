package io.dataease.service.message.service.strategy;

import io.dataease.auth.entity.SysUserEntity;
import io.dataease.auth.service.AuthUserService;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.dingtalk.service.DingtalkXpackService;
import io.dataease.service.message.service.SendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("sendDingtalk")
public class SendDingtalk implements SendService {

    @Autowired
    private AuthUserService authUserService;

    @Override
    public void sendMsg(Long userId, Long typeId, String content, String param) {
        SysUserEntity userEntity = authUserService.getUserById(userId);

        if (userEntity.getFrom() == 5 && authUserService.supportDingtalk()) {
            String username = userEntity.getUsername();
            DingtalkXpackService dingtalkXpackService = SpringContextUtil.getBean(DingtalkXpackService.class);
            List<String> userIds = new ArrayList<>();
            userIds.add(username);
            dingtalkXpackService.pushMsg(userIds, content);
        }
    }
}
