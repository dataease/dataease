package io.dataease.service.message.service.strategy;

import io.dataease.auth.service.AuthUserService;
import io.dataease.plugins.common.base.domain.SysUserAssist;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.wecom.service.WecomXpackService;
import io.dataease.service.message.service.SendService;
import io.dataease.service.sys.SysUserService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service("sendWecom")
public class SendWecom implements SendService {

    @Autowired
    private AuthUserService authUserService;

    @Resource
    private SysUserService sysUserService;

    @Override
    public void sendMsg(Long userId, Long typeId, String content, String param) {
        SysUserAssist sysUserAssist = sysUserService.assistInfo(userId);
        if (ObjectUtils.isNotEmpty(sysUserAssist) && StringUtils.isNotBlank(sysUserAssist.getWecomId()) && authUserService.supportWecom()) {

            String username = sysUserAssist.getWecomId();
            WecomXpackService wecomXpackService = SpringContextUtil.getBean(WecomXpackService.class);
            List<String> userIds = new ArrayList<>();
            userIds.add(username);
            wecomXpackService.pushMsg(userIds, content);
        }
    }
}
