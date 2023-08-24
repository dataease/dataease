package io.dataease.plugins.xpack.display.dto.response;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SysSettingDto {

    private String paramKey;

    private String paramValue;

    private String type;

    private Integer sort;

    private MultipartFile file;

    private String fileName;

    private Long blobId;

}
