package io.dataease.controller;



import com.google.gson.Gson;
import io.dataease.commons.license.DefaultLicenseService;
import io.dataease.commons.license.F2CLicenseResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;

@RestController
@RequestMapping(headers = "Accept=application/json")
public class LicenseController {

    @Value("${dataease.need_validate_lic:true}")
    private Boolean need_validate_lic;

    @Resource
    private DefaultLicenseService defaultLicenseService;

    @GetMapping(value = "anonymous/license/validate")
    public ResultHolder validateLicense() throws Exception {
//        if (!need_validate_lic) {
//            return ResultHolder.success(null);
//        }
        F2CLicenseResponse f2CLicenseResponse = defaultLicenseService.validateLicense();
        System.out.println(new Gson().toJson(f2CLicenseResponse));
        switch (f2CLicenseResponse.getStatus()) {
            case no_record:
                return ResultHolder.success(f2CLicenseResponse);
            case valid:
                return ResultHolder.success(null);
            case expired:
                String expired = f2CLicenseResponse.getLicense().getExpired();
                throw new Exception("License has expired since " + expired + ", please update license.");
            case invalid:
                throw new Exception(f2CLicenseResponse.getMessage());
            default:
                throw new Exception("Invalid License.");
        }
    }
}
