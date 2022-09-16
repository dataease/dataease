package io.dataease.plugins.server;


import io.dataease.auth.entity.SysUserEntity;
import io.dataease.auth.entity.TokenInfo;
import io.dataease.auth.service.AuthUserService;
import io.dataease.auth.util.JWTUtils;
import io.dataease.commons.constants.SysLogConstants;
import io.dataease.commons.exception.DEException;
import io.dataease.commons.utils.DeLogUtils;
import io.dataease.commons.utils.LogUtil;
import io.dataease.commons.utils.ServletUtils;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.display.dto.response.SysSettingDto;

import io.dataease.plugins.xpack.wecom.dto.entity.BaseQrResult;
import io.dataease.plugins.xpack.wecom.dto.entity.WecomAuthResult;
import io.dataease.plugins.xpack.wecom.dto.response.WecomInfo;
import io.dataease.plugins.xpack.wecom.service.WecomXpackService;
import io.dataease.service.sys.SysUserService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@ApiIgnore
@RequestMapping("/plugin/wecom")
@Controller
public class XWecomServer {

    @Resource
    private AuthUserService authUserService;
    @Resource
    private SysUserService sysUserService;

    @ResponseBody
    @GetMapping("/info")
    public WecomInfo getWecomInfo() {
        WecomXpackService wecomXpackService = SpringContextUtil.getBean(WecomXpackService.class);
        return wecomXpackService.info();
    }
    @ResponseBody
    @RequiresPermissions("sysparam:read")
    @PostMapping("/save")
    public void save(@RequestBody List<SysSettingDto> settings) {
        WecomXpackService wecomXpackService = SpringContextUtil.getBean(WecomXpackService.class);
        wecomXpackService.save(settings);
    }
    @ResponseBody
    @PostMapping("/testConn")
    public void testConn(@RequestBody WecomInfo wecomInfo) {
        WecomXpackService wecomXpackService = SpringContextUtil.getBean(WecomXpackService.class);
        try {
            wecomXpackService.testConn(wecomInfo);
        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    @ResponseBody
    @PostMapping("/getQrParam")
    public BaseQrResult getQrParam() {
        WecomXpackService wecomXpackService = SpringContextUtil.getBean(WecomXpackService.class);
        return wecomXpackService.getQrParam();
    }

    @GetMapping("/callBack")
    public ModelAndView callBack(@RequestParam("code") String code, @RequestParam("state") String state) {
        ModelAndView modelAndView = new ModelAndView("redirect:/");
        HttpServletResponse response = ServletUtils.response();
        WecomXpackService wecomXpackService = null;
        try {
            Map<String, WecomXpackService> beansOfType = SpringContextUtil.getApplicationContext().getBeansOfType((WecomXpackService.class));
            if (beansOfType.keySet().size() == 0) {
                DEException.throwException("缺少企业微信插件");
            }
            wecomXpackService = SpringContextUtil.getBean(WecomXpackService.class);
            Boolean isOpen = wecomXpackService.isOpen();
            if (!isOpen) {
                DEException.throwException("未开启企业微信");
            }
            WecomAuthResult authResult = wecomXpackService.auth(code);

            String userId = authResult.getUserId();
            Map<String, Object> userMap = wecomXpackService.userInfo(userId);

            SysUserEntity sysUserEntity = authUserService.getUserBySub(userId, 4);
            if (null == sysUserEntity) {
                Object emailObj = ObjectUtils.isEmpty(userMap.get("biz_mail")) ? userMap.get("email") : userMap.get("biz_mail");
                String email = ObjectUtils.isEmpty(emailObj) ? "demo@wecom.work" : emailObj.toString();
                sysUserService.validateExistUser(userId, userMap.get("name").toString(), email);
                sysUserService.saveWecomCUser(userMap, userId, email);
                sysUserEntity = authUserService.getUserBySub(userId, 4);
            }
            TokenInfo tokenInfo = TokenInfo.builder().userId(sysUserEntity.getUserId()).username(sysUserEntity.getUsername()).build();
            String realPwd = sysUserEntity.getPassword();
            String token = JWTUtils.sign(tokenInfo, realPwd);
            ServletUtils.setToken(token);

            DeLogUtils.save(SysLogConstants.OPERATE_TYPE.LOGIN, SysLogConstants.SOURCE_TYPE.USER, sysUserEntity.getUserId(), null, null, null);

            Cookie cookie_token = new Cookie("Authorization", token);
            cookie_token.setPath("/");

            response.addCookie(cookie_token);

        } catch (Exception e) {

            String msg = e.getMessage();
            if (null != e.getCause()) {
                msg = e.getCause().getMessage();
            }
            try {
                msg = URLEncoder.encode(msg, "UTF-8");
                LogUtil.error(e);
                Cookie cookie_error = new Cookie("WecomError", msg);
                cookie_error.setPath("/");
                response.addCookie(cookie_error);
                return modelAndView;
            } catch (UnsupportedEncodingException e1) {
                e.printStackTrace();
            }


        }
        return modelAndView;
    }
}
