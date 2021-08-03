package io.dataease.controller.sys;


import io.dataease.commons.constants.I18nConstants;
import io.dataease.commons.exception.DEException;
import io.dataease.commons.utils.LogUtil;
import io.dataease.i18n.Lang;
import io.dataease.i18n.Translator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by liqiang on 2019/4/1.
 */
@ApiIgnore
@RestController
public class I18nController {

    private static final int FOR_EVER = 3600 * 24 * 30 * 12 * 10; //10 years in second

    @Value("${run.mode:release}")
    private String runMode;


    @GetMapping("lang/change/{lang}")
    public void changeLang(@PathVariable String lang, HttpServletRequest request, HttpServletResponse response) {
        Lang targetLang = Lang.getLangWithoutDefault(lang);
        if (targetLang == null) {
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            LogUtil.error("Invalid parameter: " + lang);
            DEException.throwException(Translator.get("error_lang_invalid"));
        }
        Cookie cookie = new Cookie(I18nConstants.LANG_COOKIE_NAME, targetLang.getDesc());
        cookie.setPath("/");
        cookie.setMaxAge(FOR_EVER);
        response.addCookie(cookie);
        //重新登录
        if ("release".equals(runMode)) {
            Cookie f2cCookie = new Cookie("DE_SESSION_ID", "deleteMe");
            f2cCookie.setPath("/");
            f2cCookie.setMaxAge(0);
            response.addCookie(f2cCookie);
        }
        //本地测试用
        if ("local".equals(runMode)) {
            if (request != null) {
                request.getSession(true).setAttribute(I18nConstants.LANG_COOKIE_NAME, lang);
            }
        }
    }
}
