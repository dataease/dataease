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
import io.dataease.plugins.xpack.lark.dto.entity.LarkQrResult;
import io.dataease.plugins.xpack.lark.dto.response.LarkInfo;
import io.dataease.plugins.xpack.larksuite.dto.entity.UserData;
import io.dataease.plugins.xpack.larksuite.dto.response.LarksuiteUserResult;
import io.dataease.plugins.xpack.larksuite.service.LarksuiteXpackService;
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
@RequestMapping("/plugin/larksuite")
@Controller
public class XLarksuiteServer {

    @Resource
    private AuthUserService authUserService;
    @Resource
    private SysUserService sysUserService;

    @ResponseBody
    @GetMapping("/info")
    public LarkInfo getLarkInfo() {
        LarksuiteXpackService larkXpackService = SpringContextUtil.getBean(LarksuiteXpackService.class);
        return larkXpackService.info();
    }

    @ResponseBody
    @RequiresPermissions("sysparam:read")
    @PostMapping("/save")
    public void save(@RequestBody List<SysSettingDto> settings) {
        LarksuiteXpackService larkXpackService = SpringContextUtil.getBean(LarksuiteXpackService.class);
        larkXpackService.save(settings);
    }

    @ResponseBody
    @PostMapping("/testConn")
    public void testConn(@RequestBody LarkInfo larkInfo) {
        LarksuiteXpackService larkXpackService = SpringContextUtil.getBean(LarksuiteXpackService.class);
        try {
            larkXpackService.testConn(larkInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @ResponseBody
    @PostMapping("/getQrParam")
    public LarkQrResult getQrParam() {
        LarksuiteXpackService larkXpackService = SpringContextUtil.getBean(LarksuiteXpackService.class);
        return larkXpackService.getQrParam();
    }

    @GetMapping("/callBack")
    public ModelAndView callBack(@RequestParam("code") String code, @RequestParam("state") String state) {
        ModelAndView modelAndView = new ModelAndView("redirect:/");
        HttpServletResponse response = ServletUtils.response();
        LarksuiteXpackService larksuiteXpackService = null;
        try {
            Map<String, LarksuiteXpackService> beansOfType = SpringContextUtil.getApplicationContext().getBeansOfType((LarksuiteXpackService.class));
            if (beansOfType.keySet().size() == 0) {
                DEException.throwException("缺少国际飞书插件");
            }
            larksuiteXpackService = SpringContextUtil.getBean(LarksuiteXpackService.class);
            Boolean isOpen = larksuiteXpackService.isOpen();
            if (!isOpen) {
                DEException.throwException("未开启国际飞书");
            }
            LarksuiteUserResult larksuiteUserResult = larksuiteXpackService.userInfo(code, state, false);
            UserData larkUserInfo = larksuiteUserResult.getData();
            String username = larkUserInfo.getUser_id();
            SysUserEntity sysUserEntity = authUserService.getUserByLarksuiteId(username);
            if (null == sysUserEntity) {
                if (authUserService.checkScanCreateLimit())
                    DEException.throwException(Translator.get("I18N_PROHIBIT_SCANNING_TO_CREATE_USER"));
                String email = StringUtils.isNotBlank(larkUserInfo.getEmail()) ? larkUserInfo.getEmail() : (username + "@larksuite.work");
                sysUserService.validateExistUser(username, larkUserInfo.getName(), email);
                sysUserService.saveLarksuiteCUser(larkUserInfo, email);
                sysUserEntity = authUserService.getUserByLarksuiteId(username);
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

            response.addCookie(cookie_token);
        } catch (Exception e) {

            String msg = e.getMessage();
            if (null != e.getCause()) {
                msg = e.getCause().getMessage();
            }
            try {
                msg = URLEncoder.encode(msg, "UTF-8");
                LogUtil.error(e);
                Cookie cookie_error = new Cookie("LarksuiteError", msg);
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
        Cookie cookie_error = new Cookie("LarksuiteError", errorMsg);
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

        HttpServletResponse response = ServletUtils.response();
        String url = "/#person-info/index";

        LarksuiteXpackService larksuiteXpackService = null;
        try {
            SysUserEntity userEntity = authUserService.getUserById(Long.parseLong(state));
            if (ObjectUtils.isEmpty(userEntity)) {
                bindError(response, url, "绑定用户不存在");
                return;
            }
            SysUserAssist sysUserAssist = sysUserService.assistInfo(Long.parseLong(state));
            if (ObjectUtils.isNotEmpty(sysUserAssist) && StringUtils.isNotBlank(sysUserAssist.getLarksuiteId())) {
                bindError(response, url, "目标用户已绑定其他国际飞书账号");
                return;
            }

            Boolean isOpen = authUserService.supportLarksuite();
            if (!isOpen) {
                DEException.throwException("未开启国际飞书");
            }
            larksuiteXpackService = SpringContextUtil.getBean(LarksuiteXpackService.class);
            LarksuiteUserResult larksuiteUserResult = larksuiteXpackService.userInfo(code, state, true);
            UserData larkUserInfo = larksuiteUserResult.getData();
            String userId = larkUserInfo.getUser_id();


            SysUserEntity sysUserEntity = authUserService.getUserByLarksuiteId(userId);
            if (null != sysUserEntity) {
                bindError(response, url, "当前国际飞书账号已绑定其他DE用户");
                return;
            }

            if (ObjectUtils.isEmpty(sysUserAssist)) {
                sysUserAssist = new SysUserAssist();
                sysUserAssist.setUserId(Long.parseLong(state));
            }
            sysUserAssist.setLarksuiteId(userId);
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
