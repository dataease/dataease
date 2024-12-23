package io.dataease.i18n;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1000)
public class DeI18nStarter implements ApplicationRunner {


    @Override
    public void run(ApplicationArguments args) throws Exception {
        DynamicI18nUtils.addOrUpdate("file:/opt/dataease2.0/data/i18n/custom");
    }

}
