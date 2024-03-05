package io.dataease.listener;

import io.dataease.commons.utils.LogUtil;
import io.dataease.plugins.common.util.SpringContextUtil;
import io.dataease.plugins.xpack.proxy.service.ProxyXpackService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ProxyListener implements ApplicationListener<ApplicationReadyEvent> {
    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        try {
            Map<String, ProxyXpackService> beansOfType = SpringContextUtil.getApplicationContext().getBeansOfType((ProxyXpackService.class));
            if (beansOfType.keySet().size() == 0) {
                return;
            }
            ProxyXpackService xpackService = SpringContextUtil.getBean(ProxyXpackService.class);
            if (ObjectUtils.isNotEmpty(xpackService)) {
                xpackService.setProxy();
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }

    }
}
