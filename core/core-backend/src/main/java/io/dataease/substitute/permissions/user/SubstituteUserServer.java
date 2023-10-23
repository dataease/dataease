package io.dataease.substitute.permissions.user;


import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Component
@ConditionalOnMissingBean(name = "loginServer")
@RestController
@RequestMapping("/user")
public class SubstituteUserServer {

    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> result = new HashMap<>();
        result.put("id", "1");
        result.put("name", "管理员");
        result.put("oid", "1");
        result.put("language", "zh-CN");
        return result;
    }
}
