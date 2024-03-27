package io.dataease.controller.ai;

import io.dataease.service.ai.AiBaseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author : WangJiaHao
 * @date : 2024/3/27 09:44
 */

@RestController
@RequestMapping("aiBase")
public class AiComponentController {
    @Resource
    private AiBaseService aiBaseService;

    @GetMapping("findTargetUrl")
    Map<String, String> findTargetUrl(){
        return aiBaseService.findTargetUrl();
    }
}
