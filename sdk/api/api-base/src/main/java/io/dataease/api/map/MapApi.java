package io.dataease.api.map;

import io.dataease.api.map.vo.AreaNode;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Tag(name = "地图")
public interface MapApi {
    /**
     * 获取世界树
     * @return
     */
    @GetMapping("/worldTree")
    AreaNode getWorldTree();
}
