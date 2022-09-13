package io.dataease.service.message.service.strategy;

import io.dataease.auth.entity.SysUserEntity;
import io.dataease.auth.service.AuthUserService;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.lark.service.LarkXpackService;
import io.dataease.service.message.service.SendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("sendLark")
public class SendLark implements SendService {

    @Autowired
    private AuthUserService authUserService;

    @Override
    public void sendMsg(Long userId, Long typeId, String content, String param) {
        SysUserEntity userEntity = authUserService.getUserById(userId);

        if (userEntity.getFrom() == 6 && authUserService.supportLark()) {
            String username = userEntity.getUsername();
            LarkXpackService larkXpackService = SpringContextUtil.getBean(LarkXpackService.class);
            List<String> userIds = new ArrayList<>();
            userIds.add(username);
            larkXpackService.pushMsg(userIds, content);
        }
    }
}
