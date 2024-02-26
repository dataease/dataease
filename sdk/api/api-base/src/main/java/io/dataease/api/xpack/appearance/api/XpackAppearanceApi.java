package io.dataease.api.xpack.appearance.api;

import io.dataease.api.xpack.appearance.vo.AppearanceItemVO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface XpackAppearanceApi {

    @PostMapping(value = "/save", consumes = {"multipart/form-data"})
    void save(@RequestPart("request") List<AppearanceItemVO> items, @RequestPart(value = "files", required = false) List<MultipartFile> files);

    @GetMapping("/query")
    List<AppearanceItemVO> query();

    @GetMapping(value = "/image/{imageId}", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    ResponseEntity<byte[]> image(@PathVariable("imageId") String imageId);
}
