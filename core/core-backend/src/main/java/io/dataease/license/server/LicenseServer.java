package io.dataease.license.server;

import io.dataease.api.license.LicenseApi;
import io.dataease.api.license.dto.LicenseRequest;
import io.dataease.license.bo.F2CLicResult;
import io.dataease.license.manage.CoreLicManage;
import io.dataease.license.manage.F2CLicManage;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/license")
public class LicenseServer implements LicenseApi {

    private static final String product = "DataEase";
    @Resource
    private CoreLicManage coreLicManage;

    @Resource
    private F2CLicManage f2CLicManage;


    @Override
    public F2CLicResult update(LicenseRequest request) {
        return f2CLicManage.updateLicense(product, request.getLicense());
    }

    @Override
    public F2CLicResult validate(LicenseRequest request) {
        if (StringUtils.isBlank(request.getLicense())) {
            return f2CLicManage.validate();
        }
        F2CLicResult result = f2CLicManage.validate(product, request.getLicense());
        return result;
    }

    @Override
    public String version() {
        return coreLicManage.getVersion();
    }
}
