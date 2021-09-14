package io.dataease.plugins.server;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import io.dataease.auth.entity.SysUserEntity;
import io.dataease.auth.entity.TokenInfo;
import io.dataease.auth.service.AuthUserService;
import io.dataease.auth.util.JWTUtils;
import io.dataease.commons.exception.DEException;
import io.dataease.commons.utils.ServletUtils;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.display.dto.response.SysSettingDto;
import io.dataease.plugins.xpack.oidc.dto.SSOToken;
import io.dataease.plugins.xpack.oidc.dto.SSOUserInfo;
import io.dataease.plugins.xpack.oidc.service.OidcXpackService;
import io.dataease.service.sys.SysUserService;

@RequestMapping("/sso")
@Controller
public class SSOServer {

    @Autowired
    private AuthUserService authUserService;

    @Autowired
    private SysUserService sysUserService;

    @GetMapping("/callBack")
    public ModelAndView callBack(@RequestParam("code") String code, @RequestParam("statue") String state) {

        Map<String, OidcXpackService> beansOfType = SpringContextUtil.getApplicationContext().getBeansOfType((OidcXpackService.class));
        if(beansOfType.keySet().size() == 0) {
            DEException.throwException("缺少oidc插件");
        }
        OidcXpackService oidcXpackService = SpringContextUtil.getBean(OidcXpackService.class);
        Boolean suuportOIDC = oidcXpackService.isSuuportOIDC();
        if (!suuportOIDC) {
            DEException.throwException("未开启oidc");
        }
        SSOToken ssoToken = oidcXpackService.requestSsoToken(code, state);
        Map<String, String> config = config(oidcXpackService);
        SSOUserInfo ssoUserInfo = oidcXpackService.requestUserInfo(config, ssoToken.getAccessToken());
        SysUserEntity sysUserEntity = authUserService.getUserByName(ssoUserInfo.getUserName());
        if(null == sysUserEntity){
            sysUserService.saveOIDCUser(ssoUserInfo);
            sysUserEntity = authUserService.getUserByName(ssoUserInfo.getUserName());
        }
        TokenInfo tokenInfo = TokenInfo.builder().userId(sysUserEntity.getUserId()).username(sysUserEntity.getUsername()).idToken(ssoToken.getIdToken()).build();
        String token = JWTUtils.sign(tokenInfo, sysUserService.defaultPWD());
        ServletUtils.setToken(token);
        ModelAndView modelAndView = new ModelAndView("/");
        return modelAndView;
    }
    private Map<String, String> config(OidcXpackService oidcXpackService) {
        List<SysSettingDto> sysSettingDtos = oidcXpackService.oidcSettings();
        Map<String, String> config = sysSettingDtos.stream().collect(Collectors.toMap(SysSettingDto::getParamKey, SysSettingDto::getParamValue));
        return config;
    }
    

    
    
}
