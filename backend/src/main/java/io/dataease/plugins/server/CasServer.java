package io.dataease.plugins.server;

import cn.hutool.core.util.RandomUtil;
import io.dataease.auth.entity.SysUserEntity;
import io.dataease.auth.entity.TokenInfo;
import io.dataease.auth.service.AuthUserService;
import io.dataease.auth.util.JWTUtils;
import io.dataease.commons.constants.SysLogConstants;
import io.dataease.commons.utils.CodingUtil;
import io.dataease.commons.utils.DeLogUtils;
import io.dataease.commons.utils.LogUtil;
import io.dataease.commons.utils.ServletUtils;

import io.dataease.controller.ResultHolder;
import io.dataease.i18n.Translator;
import io.dataease.service.sys.SysUserService;
import io.dataease.service.system.SystemParameterService;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.util.AssertionHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@ApiIgnore
@RequestMapping("/cas")
@Controller
public class CasServer {

    @Autowired
    private AuthUserService authUserService;

    @Autowired
    private SysUserService sysUserService;

    @Resource
    private SystemParameterService systemParameterService;

    @GetMapping("/callBack")
    public ModelAndView callBack() {
        ModelAndView modelAndView = new ModelAndView("redirect:/");
        HttpServletResponse response = ServletUtils.response();

        AttributePrincipal principal = AssertionHolder.getAssertion().getPrincipal();
        String name = principal.getName();
        try {
            SysUserEntity sysUserEntity = authUserService.getCasUserByName(name);
            if(null == sysUserEntity){
                String s = RandomUtil.randomString(6);
                String email = s + "@xxx.com";
                sysUserService.validateCasUser(name);
                sysUserService.saveCASUser(name, email);
                sysUserEntity = authUserService.getUserByName(name);
            }
            String realPwd = sysUserEntity.getPassword();
            TokenInfo tokenInfo = TokenInfo.builder().userId(sysUserEntity.getUserId()).username(sysUserEntity.getUsername()).build();
            String token = JWTUtils.sign(tokenInfo, realPwd);
            ServletUtils.setToken(token);
            Cookie cookie_token = new Cookie("Authorization", token);cookie_token.setPath("/");
            response.addCookie(cookie_token);
            DeLogUtils.save(SysLogConstants.OPERATE_TYPE.LOGIN, SysLogConstants.SOURCE_TYPE.USER, sysUserEntity.getUserId(), null, null, null);

        }catch(Exception e) {

            String msg = e.getMessage();
            if (null != e.getCause()) {
                msg = e.getCause().getMessage();
            }
            try {
                msg = URLEncoder.encode(msg, "UTF-8");
                LogUtil.error(e);
                Cookie cookie_error = new Cookie("CasError", msg);
                cookie_error.setPath("/");
                response.addCookie(cookie_error);

                return modelAndView;
            } catch (UnsupportedEncodingException e1) {
                e.printStackTrace();
            }
        }
        return modelAndView;
    }

    @GetMapping("/reset/{uname}/{pwd}")
    @ResponseBody
    public ResultHolder reset(@PathVariable(value = "uname", required = true) String uname, @PathVariable(value = "pwd", required = true) String pwd) {
        try {
            authUserService.checkAdmin(uname, pwd);
            systemParameterService.resetCas();
            String token = ServletUtils.getToken();
            if (StringUtils.isNotBlank(token)) {
                Long userId = JWTUtils.tokenInfoByToken(token).getUserId();
                authUserService.clearCache(userId);
            }
            HttpServletRequest request = ServletUtils.request();
            request.getSession().invalidate();
            ResultHolder success = ResultHolder.success(null);
            success.setMessage(Translator.get("i18n_default_login_reset"));
            return success;
        }catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            ResultHolder error = ResultHolder.error(e.getMessage());
            return error;
        }
    }
}
