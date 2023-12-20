package io.dataease.api.map;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.map.dto.GeometryNodeCreator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "系统设置:地理信息")
@ApiSupport(order = 798)
public interface GeoApi {

    @Operation(summary = "保存地理信息")
    @PostMapping(value = "/save", consumes = {"multipart/form-data"})
    void saveMapGeo(@RequestPart("request") GeometryNodeCreator request, @RequestPart(value = "file") MultipartFile file);

    @Operation(summary = "删除地理信息")
    @PostMapping("/delete/{id}")
    void deleteGeo(@PathVariable("id") String id);
}
