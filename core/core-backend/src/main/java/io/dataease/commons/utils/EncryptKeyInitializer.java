package io.dataease.commons.utils;

import io.dataease.rsa.manage.RsaManage;
import jakarta.annotation.Resource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class EncryptKeyInitializer implements ApplicationRunner {

    @Resource
    private RsaManage rsaManage;

    @Override
    public void run(ApplicationArguments args) {
        rsaManage.check();
        String aesKey = rsaManage.query().getAesKey();
        EncryptUtils.initKeys(aesKey);
    }
}
