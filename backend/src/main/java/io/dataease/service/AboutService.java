package io.dataease.service;

import io.dataease.commons.license.DefaultLicenseService;
import io.dataease.commons.license.F2CLicenseResponse;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.commons.utils.LogUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.Optional;

@Service
public class AboutService {
    private static final String BUILD_VERSION = "/opt/dataease/conf/version";
    private static final String product = "DataEase";

    @Resource
    private DefaultLicenseService defaultLicenseService;

    public F2CLicenseResponse updateLicense(String licenseKey) {
        F2CLicenseResponse f2CLicenseResponse = defaultLicenseService.updateLicense(product, licenseKey);
        return f2CLicenseResponse;
    }

    public F2CLicenseResponse validateLicense(String licenseKey) {
        if (StringUtils.isNotBlank(licenseKey)) {
            return defaultLicenseService.validateLicense(product, licenseKey);
        } else {
            return defaultLicenseService.validateLicense();
        }
    }

    public String getBuildVersion() {
        try {
            File file = new File(BUILD_VERSION);
            if (file.exists()) {
                String version = FileUtils.readFileToString(file, "UTF-8");
                if (StringUtils.isNotBlank(version)) {
                    return version;
                }
            }
            String property = CommonBeanFactory.getBean(Environment.class).getProperty("cmp.version");
            String result = Optional.ofNullable(property).orElse("V1.0");
            return result;
        } catch (Exception e) {
            LogUtil.error("failed to get build version.", e);
        }
        return "unknown";
    }
}
