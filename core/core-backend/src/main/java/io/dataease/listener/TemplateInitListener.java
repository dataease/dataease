package io.dataease.listener;

import io.dataease.license.utils.LogUtil;
import io.dataease.template.manage.TemplateLocalParseManage;
import jakarta.annotation.Resource;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(value = 3)
public class TemplateInitListener implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private TemplateLocalParseManage templateLocalParseManage;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        LogUtil.info("=====Template init from code [Start]=====");
        try{
            templateLocalParseManage.doInit();
        }catch (Exception e){
            LogUtil.error("=====Template init from code ERROR=====");
        }
        LogUtil.info("=====Template init from code [End]=====");
    }
}
