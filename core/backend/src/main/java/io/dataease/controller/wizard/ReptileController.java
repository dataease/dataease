package io.dataease.controller.wizard;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.service.wizard.ReptileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.util.List;

@Api(tags = "首页")
@ApiSupport(order = 80)
@RestController
@RequestMapping("Reptile")
public class ReptileController {

    @Resource
    private ReptileService reptileService;

    @GetMapping("lastActive")
    @ApiOperation("获取官方Blog最新动态")
    public List lastActive() {
        return reptileService.lastActive();
    }
}
