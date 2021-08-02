package io.dataease.controller.sys;


import io.dataease.commons.license.F2CLicenseResponse;
import io.dataease.service.AboutService;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.util.Map;

@ApiIgnore
@RequestMapping("/about")
@RestController
public class AboutController {

    @Resource
    private AboutService aboutService;

    @PostMapping("/license/update")
    public F2CLicenseResponse updateLicense(@RequestBody Map<String, String> map) {
        return aboutService.updateLicense(map.get("license"));
    }

    @PostMapping("/license/validate")
    public F2CLicenseResponse validateLicense(@RequestBody Map<String, String> map) {
        return aboutService.validateLicense(map.get("license"));
    }

    @GetMapping("/build/version")
    public Object getBuildVersion() {
        return aboutService.getBuildVersion();
    }
}
