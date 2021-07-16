package io.dataease.map.api;

import io.dataease.map.dto.entity.AreaEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/map")
public interface MapApi {

    @GetMapping("/resourceFull/{areaCode}")
    String resourceFull(@PathVariable String areaCode);

    @GetMapping("/asyncGeometry")
    String asyncGeometry();

    @GetMapping("/areaEntitys/{pcode}")
    List<AreaEntity>  areaEntitys(@PathVariable String pcode);


    /**
     * 由于api有限流机制
     * 请求失败后 调用重试方法
     * @param areaCode
     */
    @GetMapping("/retry/{areaCode}")
    void retry(@PathVariable String areaCode);
}
