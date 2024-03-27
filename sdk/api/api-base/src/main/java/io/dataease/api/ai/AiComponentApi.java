package io.dataease.api.ai;

import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

/**
 * @author : WangJiaHao
 * @date : 2024/3/27 09:44
 */
public interface AiComponentApi {
    @GetMapping("findTargetUrl")
    Map<String, String> findTargetUrl();
}
