package io.dataease.license.utils;

import cn.hutool.core.collection.ListUtil;
import io.dataease.license.bo.F2CLicResult;
import io.dataease.license.config.LicenseValidator;
import io.dataease.utils.CommonBeanFactory;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LicenseUtil {

    private static List<Map.Entry<String, LicenseValidator>> validatorList;

    private static final ThreadLocal<F2CLicResult> LIC_RESULT = new ThreadLocal<F2CLicResult>();


    static {
        Map<String, LicenseValidator> validatorMap = CommonBeanFactory.getApplicationContext().getBeansOfType(LicenseValidator.class);
        sort(validatorMap);
    }

    private static void sort(Map<String, LicenseValidator> validatorMap) {
        validatorList = new ArrayList<>();
        Set<Map.Entry<String, LicenseValidator>> entries = validatorMap.entrySet();
        validatorList.addAll(entries);
        ListUtil.sort(validatorList, (a, b) -> a.getKey().compareTo(b.getKey()));
    }

    public static boolean validate() {

        for (Map.Entry<String, LicenseValidator> entry : validatorList) {
            if (StringUtils.equals("licenseValidator", entry.getKey())) continue;
            LicenseValidator validator = entry.getValue();
            boolean flag = validator.validate();
            if (!flag) {
                return false;
            }
        }
        return true;
    }

    public static void set(F2CLicResult result) {
        LIC_RESULT.set(result);
    }

    public static F2CLicResult get() {
        return LIC_RESULT.get();
    }

    public static boolean licenseValid() {
        F2CLicResult licResult = null;
        if (ObjectUtils.isEmpty(licResult = get())) return false;
        return licResult.getStatus() == F2CLicResult.Status.valid;
    }
}
