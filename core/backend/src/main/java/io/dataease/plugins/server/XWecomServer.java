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
import io.dataease.exception.DataEaseException;
import io.dataease.i18n.Translator;
import io.dataease.plugins.common.base.domain.SysUserAssist;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.display.dto.response.SysSettingDto;

import io.dataease.plugins.xpack.wecom.dto.entity.BaseQrResult;
import io.dataease.plugins.xpack.wecom.dto.entity.WecomAuthResult;
import io.dataease.plugins.xpack.wecom.dto.response.WecomInfo;
import io.dataease.plugins.xpack.wecom.service.WecomXpackService;
import io.dataease.service.sys.SysUserService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @ResponseBody
    @PostMapping("/getQrParam")
    public BaseQrResult getQrParam() {
        WecomXpackService wecomXpackService = SpringContextUtil.getBean(WecomXpackService.class);
        return wecomXpackService.getQrParam();
    }

    @GetMapping("/callBackWithoutLogin")
    public ModelAndView callBackWithoutLogin(@RequestParam("code") String code,@RequestParam("state") String state) {
        return privateCallBack(code, state, true);
    }

    @GetMapping("/callBack")
    public ModelAndView callBack(@RequestParam("code") String code, @RequestParam("state") String state) {
        return privateCallBack(code, state, false);
    }

    private ModelAndView privateCallBack(String code, String state, Boolean withoutLogin) {
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

            SysUserEntity sysUserEntity = authUserService.getUserByWecomId(userId);
            if (null == sysUserEntity) {
                if (authUserService.checkScanCreateLimit())
                    DEException.throwException(Translator.get("I18N_PROHIBIT_SCANNING_TO_CREATE_USER"));
                Object emailObj = ObjectUtils.isEmpty(userMap.get("biz_mail")) ? userMap.get("email") : userMap.get("biz_mail");
                String email = ObjectUtils.isEmpty(emailObj) ? (userId + "@wecom.work") : emailObj.toString();
                sysUserService.validateExistUser(userId, userMap.get("name").toString(), email);
                sysUserService.saveWecomCUser(userMap, userId, email);
                sysUserEntity = authUserService.getUserByWecomId(userId);
            } else if (sysUserEntity.getEnabled() == 0) {
                DataEaseException.throwException(Translator.get("i18n_user_is_disable"));
            }
            TokenInfo tokenInfo = TokenInfo.builder().userId(sysUserEntity.getUserId()).username(sysUserEntity.getUsername()).build();
            String realPwd = sysUserEntity.getPassword();
            String token = JWTUtils.sign(tokenInfo, realPwd);
            ServletUtils.setToken(token);

            DeLogUtils.save(SysLogConstants.OPERATE_TYPE.LOGIN, SysLogConstants.SOURCE_TYPE.USER, sysUserEntity.getUserId(), null, null, null);

            Cookie cookie_token = new Cookie("Authorization", token);
            cookie_token.setPath("/");

            if (withoutLogin) {
                Cookie platformCookie = new Cookie("inOtherPlatform", "true");
                platformCookie.setPath("/");
                response.addCookie(platformCookie);
            }

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

    private void bindError(HttpServletResponse response, String url, String errorMsg) {
        Cookie cookie_error = new Cookie("WecomError", errorMsg);
        cookie_error.setPath("/");
        response.addCookie(cookie_error);
        try {
            response.sendRedirect(url);
        } catch (IOException e) {
            LogUtil.error(e.getMessage(), e);
            DEException.throwException(e);
        }
    }


    @GetMapping("/bind")
    public void bind(@RequestParam("code") String code, @RequestParam("state") String state) {
        String url = "/#person-info/index";
        HttpServletResponse response = ServletUtils.response();

        WecomXpackService wecomXpackService = null;
        try {

            SysUserEntity userEntity = authUserService.getUserById(Long.parseLong(state));
            if (ObjectUtils.isEmpty(userEntity)) {
                bindError(response, url, "绑定用户不存在");
                return;
            }
            SysUserAssist sysUserAssist = sysUserService.assistInfo(Long.parseLong(state));
            if (ObjectUtils.isNotEmpty(sysUserAssist) && StringUtils.isNotBlank(sysUserAssist.getWecomId())) {
                bindError(response, url, "目标用户已绑定其他企业微信账号");
                return;
            }

            Boolean supportWecom = authUserService.supportWecom();
            if (!supportWecom) {
                DEException.throwException("未开启企业微信");
                return;
            }
            wecomXpackService = SpringContextUtil.getBean(WecomXpackService.class);
            WecomAuthResult authResult = wecomXpackService.auth(code);
            String userId = authResult.getUserId();


            SysUserEntity sysUserEntity = authUserService.getUserByWecomId(userId);
            if (null != sysUserEntity) {
                bindError(response, url, "当前企业微信账号已绑定其他DE用户");
                return;
            }
            if (ObjectUtils.isEmpty(sysUserAssist)) {
                sysUserAssist = new SysUserAssist();
                sysUserAssist.setUserId(Long.parseLong(state));
            }
            sysUserAssist.setWecomId(userId);
            sysUserService.saveAssist(sysUserAssist.getUserId(), sysUserAssist.getWecomId(), sysUserAssist.getDingtalkId(), sysUserAssist.getLarkId(), sysUserAssist.getLarksuiteId());

            response.sendRedirect(url);
        } catch (Exception e) {

            String msg = e.getMessage();
            if (null != e.getCause()) {
                msg = e.getCause().getMessage();
            }
            try {
                msg = URLEncoder.encode(msg, "UTF-8");
                LogUtil.error(e);
                bindError(response, url, msg);
            } catch (UnsupportedEncodingException e1) {
                e.printStackTrace();
            }
        }
    }
}
