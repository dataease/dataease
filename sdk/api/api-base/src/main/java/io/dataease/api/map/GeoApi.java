package io.dataease.api.map;

import io.dataease.api.map.dto.GeometryNodeCreator;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "地理信息")
public interface GeoApi {

    @PostMapping(value = "/save", consumes = {"multipart/form-data"})
    void saveMapGeo(@RequestPart("request") GeometryNodeCreator request, @RequestPart(value = "file") MultipartFile file);

    @PostMapping("/delete/{id}")
    void deleteGeo(@PathVariable("id") String id);
}
