package io.dataease.license.validator;

import io.dataease.license.config.LicenseValidator;
import io.dataease.utils.LogUtil;
import org.springframework.stereotype.Service;

// @Service
public class UserLimitValidator implements LicenseValidator {
    @Override
    public boolean validate() {
        LogUtil.info("user limit validator executing");
        return true;
    }
}
