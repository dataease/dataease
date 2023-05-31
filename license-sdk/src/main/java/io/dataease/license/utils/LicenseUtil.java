package io.dataease.license.utils;

import io.dataease.license.config.LicenseValidator;
import io.dataease.utils.CommonBeanFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class LicenseUtil {

    private static Map<String, LicenseValidator> validatorMap;


    static {
        validatorMap = CommonBeanFactory.getApplicationContext().getBeansOfType(LicenseValidator.class);
    }

    public static boolean validate() {
        for (Map.Entry<String, LicenseValidator> entry : validatorMap.entrySet()) {
            if (StringUtils.equals("licenseValidator", entry.getKey())) continue;
            LicenseValidator validator = entry.getValue();
            boolean flag = validator.validate();
            if (!flag) {
                return false;
            }
        }
        return true;
    }
}
