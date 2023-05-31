package io.dataease.api.license;

import io.dataease.api.license.dto.LicenseRequest;
import io.dataease.license.bo.F2CLicResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface LicenseApi {


    @PostMapping("/update")
    F2CLicResult update(@RequestBody LicenseRequest request);

    @PostMapping("/validate")
    F2CLicResult validate(@RequestBody LicenseRequest request);

    @GetMapping("/version")
    String version();
}
