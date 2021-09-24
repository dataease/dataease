package io.dataease.plugins.server;


import io.dataease.plugins.common.entity.XpackLdapUserEntity;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.display.dto.response.SysSettingDto;
import io.dataease.plugins.xpack.ldap.dto.response.LdapInfo;
import io.dataease.plugins.xpack.ldap.service.LdapXpackService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/plugin/ldap")
@RestController
public class XLdapServer {


    @GetMapping("/info")
    public LdapInfo getLdapInfo() {
        LdapXpackService ldapXpackService = SpringContextUtil.getBean(LdapXpackService.class);
        return ldapXpackService.info();
    }

    @PostMapping("/save")
    public void save(@RequestBody List<SysSettingDto> settings) {
        LdapXpackService ldapXpackService = SpringContextUtil.getBean(LdapXpackService.class);
        ldapXpackService.save(settings);
    }

    @PostMapping("/testConn")
    public void testConn() {
        LdapXpackService ldapXpackService = SpringContextUtil.getBean(LdapXpackService.class);
        try {
            ldapXpackService.testConn();
        }catch(Exception e) {
            throw new RuntimeException(e);
        } 
    }

    @PostMapping("/users")
    public List<XpackLdapUserEntity> users() {
        LdapXpackService ldapXpackService = SpringContextUtil.getBean(LdapXpackService.class);
        return ldapXpackService.users();
    }
}
