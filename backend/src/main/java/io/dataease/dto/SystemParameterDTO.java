package io.dataease.dto;

import io.dataease.plugins.common.base.domain.SystemParameter;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.web.multipart.MultipartFile;

public class SystemParameterDTO extends SystemParameter {
    @ApiModelProperty("文件")
    private MultipartFile file;
    @ApiModelProperty("文件名称")
    private String fileName;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
