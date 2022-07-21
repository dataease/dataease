package io.dataease.map.api;

import io.dataease.map.dto.entity.AreaEntity;
import io.dataease.map.dto.request.MapNodeRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/api/map")
public interface MapApi {



    @GetMapping("/areaEntitys/{pcode}")
    List<AreaEntity>  areaEntitys(@PathVariable String pcode);


    @GetMapping("/globalEntitys/{pcode}")
    List<AreaEntity>  globalEntitys(@PathVariable String pcode);

    @PostMapping(value = "/saveMapNode", consumes = {"multipart/form-data"})
    void saveMapNode(MapNodeRequest request, MultipartFile file) throws Exception;

    @PostMapping("/delMapNode")
    void delMapNode(MapNodeRequest request);
}
