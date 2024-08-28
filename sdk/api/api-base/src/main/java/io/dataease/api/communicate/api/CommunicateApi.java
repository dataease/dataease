package io.dataease.api.communicate.api;

import io.dataease.api.communicate.dto.MessageDTO;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Hidden
public interface CommunicateApi {

    @PostMapping("/send")
    void send(@RequestBody MessageDTO dto);

    @GetMapping("/down/{fileId}/{fileName}/{suffix}")
    ResponseEntity<ByteArrayResource> down(@PathVariable("fileId") String fileId, @PathVariable("fileName") String fileName, @PathVariable("suffix") String suffix) throws Exception;

    @GetMapping(value = "/image/{imageId}", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    ResponseEntity<byte[]> image(@PathVariable("imageId") String imageId);
}
