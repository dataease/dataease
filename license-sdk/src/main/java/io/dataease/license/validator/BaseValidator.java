package io.dataease.license.validator;

import io.dataease.license.bo.F2CLicResult;
import io.dataease.license.config.LicenseValidator;
import io.dataease.license.manage.F2CLicManage;
import io.dataease.license.utils.LicenseUtil;
import io.dataease.utils.LogUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component("1-validator")
public class BaseValidator implements LicenseValidator {

    @Resource
    private F2CLicManage f2CLicManage;

    @Override
    public boolean validate() {
        LogUtil.info("license validator executing");
        F2CLicResult f2CLicResult = f2CLicManage.validate();
        LicenseUtil.set(f2CLicResult);
        // return f2CLicResult.getStatus() ==  F2CLicResult.Status.valid;
        return true;
    }
}
