package io.dataease.license.validator;

import io.dataease.license.config.LicenseValidator;
import io.dataease.license.manage.F2CLicManage;
import io.dataease.utils.LogUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

// @Service
public class BaseValidator implements LicenseValidator {

    @Resource
    private F2CLicManage f2CLicManage;

    @Override
    public boolean validate() {
        LogUtil.info("license validator executing");
        return true ;
    }
}
