package io.dataease.controller.sys;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.dataease.auth.filter.F2CLinkFilter;
import io.dataease.dto.UserLoginInfoDTO;
import io.dataease.service.SystemInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@ApiIgnore
@RestController
@RequestMapping("systemInfo")
public class SystemInfoController {
    @Resource
    private SystemInfoService systemInfoService;

    @GetMapping("userLoginInfo")
    public UserLoginInfoDTO userLoginInfo() throws IOException {
        return systemInfoService.getUserLoginInfo(null);
    }

    @GetMapping("proxyUserLoginInfo")
    public UserLoginInfoDTO proxyUserLoginInfo() throws IOException {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        String linkToken = request.getHeader(F2CLinkFilter.LINK_TOKEN_KEY);
        if (StringUtils.isNotEmpty(linkToken)) {
            DecodedJWT jwt = JWT.decode(linkToken);
            return systemInfoService.getUserLoginInfo(jwt.getClaim("userId").asLong());
        } else {
            return null;
        }
        
    }
}
