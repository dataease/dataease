package io.dataease.api.map;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.map.vo.AreaNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Tag(name = "系统设置:地图区域")
@ApiSupport(order = 797)
public interface MapApi {
    /**
     * 获取世界树
     * @return
     */
    @Operation(summary = "查询区域树")
    @GetMapping("/worldTree")
    AreaNode getWorldTree();
}
