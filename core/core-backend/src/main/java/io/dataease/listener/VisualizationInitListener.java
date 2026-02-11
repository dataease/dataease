package io.dataease.listener;

import io.dataease.license.utils.LogUtil;
import io.dataease.template.manage.TemplateLocalParseManage;
import io.dataease.visualization.manage.CoreVisualizationManage;
import jakarta.annotation.Resource;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(value = 4)
public class VisualizationInitListener implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private CoreVisualizationManage coreVisualizationManage;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        try{
            coreVisualizationManage.dataVisualizationInit();
        }catch (Exception e){
            LogUtil.error("=====Visualization init from code ERROR=====");
        }
        LogUtil.info("=====Visualization init from code [End]=====");
    }
}
