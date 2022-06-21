package io.dataease.controller;

import io.dataease.commons.exception.DEException;
import io.dataease.commons.license.DefaultLicenseService;
import io.dataease.commons.license.F2CLicenseResponse;
import io.dataease.commons.utils.CodingUtil;
import io.dataease.commons.utils.LogUtil;
import io.dataease.commons.utils.ServletUtils;
import io.dataease.service.panel.PanelLinkService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping
public class IndexController {

    private static final int FOR_EVER = 3600 * 24 * 30 * 12 * 10; // 10 years in second
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
//                return "nolic.html";
                return "doc.html";
        }
    }

    @GetMapping("/link/{index}")
    public void link(@PathVariable(value = "index", required = true) String index) {
        String url;
        if (CodingUtil.isNumeric(index)) {
            url = panelLinkService.getUrlByIndex(Long.parseLong(index));
        } else {
            url = panelLinkService.getUrlByUuid(index);
        }
        HttpServletResponse response = ServletUtils.response();
        try {
            // TODO 增加仪表板外部参数
            HttpServletRequest request = ServletUtils.request();
            String attachParams = request.getParameter("attachParams");
            if(StringUtils.isNotEmpty(attachParams)){
                url = url+"&attachParams="+attachParams;
            }
            response.sendRedirect(url);
        } catch (IOException e) {
            LogUtil.error(e.getMessage());
            DEException.throwException(e);
        }
    }

    @GetMapping("/tempMobileLink/{id}/{token}")
    public void tempMobileLink(@PathVariable("id") String id, @PathVariable("token") String token) {
        String url = "/#preview/" + id;
        HttpServletResponse response = ServletUtils.response();
        Cookie cookie = new Cookie("Authorization", token);
        cookie.setPath("/");
        cookie.setMaxAge(FOR_EVER);
        response.addCookie(cookie);
        try {
            response.sendRedirect(url);
        } catch (IOException e) {
            LogUtil.error(e.getMessage());
            DEException.throwException(e);
        }
    }

}
