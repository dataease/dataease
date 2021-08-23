package io.dataease.controller;

import io.dataease.commons.license.DefaultLicenseService;
import io.dataease.commons.license.F2CLicenseResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

@Controller
@RequestMapping
public class IndexController {

    @Resource
    private DefaultLicenseService defaultLicenseService;

    @GetMapping(value = "/")
    public String index() {
        return "index.html";
    }

    @GetMapping(value = "/login")
    public String login() {
        return "index.html";
    }

    @GetMapping("/link")
    public String link() {
        return "link.html";
    }

    @GetMapping("/test")
    public String test() {
        return "test.html";
    }

    @GetMapping("/deApi")
    public String deApi() {
        F2CLicenseResponse f2CLicenseResponse = defaultLicenseService.validateLicense();
        switch (f2CLicenseResponse.getStatus()) {
            case valid:
                return "doc.html";
            default:
                // DataEaseException.throwException("Invalid License.");
                // return "nolic.html";
                return "doc.html";
        }
        // return "index.html";
    }


}
