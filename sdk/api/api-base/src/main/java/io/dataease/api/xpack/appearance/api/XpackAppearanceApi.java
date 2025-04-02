package io.dataease.api.xpack.appearance.api;

import io.dataease.api.xpack.appearance.vo.AppearanceItemVO;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "外观设置")
public interface XpackAppearanceApi {

    @Operation(summary = "保存")
    @PostMapping(value = "/save", consumes = {"multipart/form-data"})
    void save(@RequestPart("request") List<AppearanceItemVO> items, @RequestPart(value = "files", required = false) List<MultipartFile> files);

    @Operation(summary = "查询")
    @GetMapping("/query")
    List<AppearanceItemVO> query();

    @Hidden
    @GetMapping(value = "/image/{imageId}", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, "image/svg+xml"})
    ResponseEntity<byte[]> image(@PathVariable("imageId") String imageId);
}
