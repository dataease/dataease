package io.dataease.i18n;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1000)
public class DeI18nStarter implements ApplicationRunner {

    @Value("${dataease.path.i18n:file:/opt/dataease2.0/data/i18n/custom}")
    private String i18nPath;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        DynamicI18nUtils.addOrUpdate(i18nPath);
    }

}
