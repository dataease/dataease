package io.dataease.listener;

import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.cas.service.CasXpackService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;
import java.util.Map;

@Component
public class CasStatusListener implements ApplicationListener<ApplicationReadyEvent> {



    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Map<String, CasXpackService> beansOfType = SpringContextUtil.getApplicationContext().getBeansOfType((CasXpackService.class));
        if (beansOfType.keySet().size() == 0) return;
        CasXpackService casXpackService = SpringContextUtil.getBean(CasXpackService.class);
        if (ObjectUtils.isEmpty(casXpackService)) return;
        ServletContext servletContext = event.getApplicationContext().getBean(ServletContext.class);
        casXpackService.checkCasStatus(servletContext);
    }
}
