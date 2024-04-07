package io.dataease.service.ai;

import io.dataease.commons.utils.UrlTestUtils;
import io.dataease.service.system.SystemParameterService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : WangJiaHao
 * @date : 2024/3/27 09:47
 */
@Service
public class AiBaseService {
    @Resource
    private SystemParameterService parameterService;

    public Map<String, String> findTargetUrl() {
        String baseUrl = parameterService.getValue("ai.baseUrl");
        Map<String,String> result = new HashMap<>();

        if(StringUtils.isNotEmpty(baseUrl) && UrlTestUtils.isURLAvailable(baseUrl)){
            result.put("ai.baseUrl",baseUrl);

        }
        return result;
    }
}
