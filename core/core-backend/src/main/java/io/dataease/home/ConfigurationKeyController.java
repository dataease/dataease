package io.dataease.home;

import io.dataease.config.ConfigurationTransport;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ConfigurationKeyController {
    @Resource
    private ConfigurationTransport transport;

    @GetMapping("/configurationKey")
    public Map<String, Object> key(@RequestParam String path, jakarta.servlet.http.HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        return transport.issue(path);
    }
}
