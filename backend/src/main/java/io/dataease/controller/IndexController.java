package io.dataease.controller;

import io.dataease.commons.license.DefaultLicenseService;
import io.dataease.commons.license.F2CLicenseResponse;
import io.dataease.commons.utils.ServletUtils;
import io.dataease.service.panel.PanelLinkService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping
public class IndexController {

    @Resource
    private DefaultLicenseService defaultLicenseService;

    @Resource
    private PanelLinkService panelLinkService;

    @GetMapping(value = "/")
    public String index() {
        return "index.html";
    }

    @GetMapping(value = "/login")
    public String login() {
        return "index.html";
    }


    @GetMapping("/deApi")
    public String deApi() {
        F2CLicenseResponse f2CLicenseResponse = defaultLicenseService.validateLicense();
        switch (f2CLicenseResponse.getStatus()) {
            case valid:
                return "doc.html";
            default:
                return "nolic.html";
        }
    }

    @GetMapping("/link/{index}")
    public String xggznb(@PathVariable(value = "index", required = true) Long index) {
        String url = panelLinkService.getUrlByIndex(index);
        HttpServletResponse response = ServletUtils.response();
        String param = url.substring(url.indexOf("?") + 1);
        Cookie cookie = new Cookie("link", param.split("=")[1]);
        response.addCookie(cookie);
        return url;
    }


}
