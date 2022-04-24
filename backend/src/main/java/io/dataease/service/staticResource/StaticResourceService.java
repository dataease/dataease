package io.dataease.service.staticResource;

import io.dataease.commons.utils.FileUtils;
import io.dataease.exception.DataEaseException;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Author: wangjiahao
 * Date: 2022/4/24
 * Description:
 */
@Service
public class StaticResourceService {

    private final Path staticDir = Paths.get("/opt/dataease/data/static-resource/");

    public void upload(MultipartFile file) {
        // check if the path is valid (not outside staticDir)
        Assert.notNull(file, "Multipart file must not be null");
        Path uploadPath = Paths.get(staticDir.toString(), file.getOriginalFilename());
        try {
            // create dir is absent
            FileUtils.createIfAbsent(Paths.get(staticDir.toString()));
            Files.createFile(uploadPath);
            file.transferTo(uploadPath);
        } catch (IOException e) {
            DataEaseException.throwException(e);
        }
    }
}
