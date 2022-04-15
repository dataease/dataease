package io.dataease.commons.license;

import com.google.gson.Gson;
import io.dataease.plugins.common.base.domain.License;
import io.dataease.commons.exception.DEException;
import io.dataease.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class DefaultLicenseService {
    @Resource
    private InnerLicenseService innerLicenseService;

    private static final String LICENSE_ID = "fit2cloud_license";
    private static final String validatorUtil = "/usr/bin/validator";
    private static final String product = "DataEase";

    public F2CLicenseResponse validateLicense(String product, String licenseKey) {
        List<String> command = new ArrayList<String>();
        StringBuilder result = new StringBuilder();
        command.add(validatorUtil);
        command.add(licenseKey);
        try {
            execCommand(result, command);
            LogUtil.info("read lic content is : " + result.toString());
            F2CLicenseResponse f2CLicenseResponse = new Gson().fromJson(result.toString(), F2CLicenseResponse.class);
            if (f2CLicenseResponse.getStatus() != F2CLicenseResponse.Status.valid) {
                return f2CLicenseResponse;
            }
            if (!StringUtils.equals(f2CLicenseResponse.getLicense().getProduct(), product)) {
                f2CLicenseResponse.setStatus(F2CLicenseResponse.Status.invalid);
                f2CLicenseResponse.setLicense(null);
                f2CLicenseResponse.setMessage("The license is unavailable for this product.");
                return f2CLicenseResponse;
            }
            return f2CLicenseResponse;
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
            return F2CLicenseResponse.noRecord();
        }
    }


    private static int execCommand(StringBuilder result, List<String> command) throws Exception {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(command);
        Process process = builder.start();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            result.append(line).append("\n");
        }
        int exitCode = process.waitFor();
        command.clear();
        return exitCode;
    }

    public F2CLicenseResponse validateLicense() {
        try {
            License license = readLicense();
            return validateLicense(product, license.getLicense());
        } catch (Exception e) {
            return F2CLicenseResponse.noRecord();
        }
    }

    public F2CLicenseResponse updateLicense(String product, String licenseKey) {
        // 验证license
        F2CLicenseResponse response = validateLicense(product, licenseKey);
        if (response.getStatus() != F2CLicenseResponse.Status.valid) {
            return response;
        }
        // 覆盖原license
        writeLicense(licenseKey, response);
        return response;
    }

    // 从数据库读取License
    public License readLicense() {
        License license = innerLicenseService.getLicense(LICENSE_ID);
        if (license == null) {
            DEException.throwException("i18n_no_license_record");
        }
        if (StringUtils.isBlank(license.getLicense())) {
            DEException.throwException("i18n_license_is_empty");
        }
        return license;
    }

    // 创建或更新License
    private void writeLicense(String licenseKey, F2CLicenseResponse response) {
        if (StringUtils.isBlank(licenseKey)) {
            DEException.throwException("i18n_license_is_empty");
        }
        License license = new License();
        license.setId(LICENSE_ID);
        license.setLicense(licenseKey);
        license.setF2cLicense(new Gson().toJson(response));
        innerLicenseService.saveLicense(license);
    }
}
