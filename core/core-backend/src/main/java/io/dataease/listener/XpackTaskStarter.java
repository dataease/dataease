package io.dataease.listener;

import io.dataease.job.schedule.DeTaskExecutor;
import io.dataease.license.utils.LicenseUtil;
import io.dataease.utils.LogUtil;
import jakarta.annotation.Resource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(value = 4)
public class XpackTaskStarter implements ApplicationRunner {

    @Resource
    private DeTaskExecutor deTaskExecutor;

    @Override
    public void run(ApplicationArguments args) {
        try {
            LicenseUtil.validate();
            deTaskExecutor.init();
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e.getCause());
        }
    }
}
