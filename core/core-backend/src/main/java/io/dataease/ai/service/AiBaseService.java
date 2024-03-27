package io.dataease.ai.service;

import io.dataease.api.ai.AiComponentApi;
import io.dataease.system.manage.SysParameterManage;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author : WangJiaHao
 * @date : 2024/3/27 09:47
 */
@RestController
@RequestMapping("aiBase")
public class AiBaseService implements AiComponentApi {
    @Resource
    private SysParameterManage sysParameterManage;

    @Override
    public Map<String, String> findTargetUrl() {
        Map<String, String> templateParams = sysParameterManage.groupVal("ai.");
        return templateParams;
    }
}
