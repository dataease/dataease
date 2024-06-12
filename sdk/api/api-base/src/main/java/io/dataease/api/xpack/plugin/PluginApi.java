package io.dataease.api.xpack.plugin;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.xpack.plugin.vo.PluginVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Tag(name = "系统设置:插件管理")
@ApiSupport(order = 2)
public interface PluginApi {

    @GetMapping("/query")
    List<PluginVO> query();

}
