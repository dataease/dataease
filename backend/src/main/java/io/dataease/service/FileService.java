package io.dataease.service;


import io.dataease.commons.constants.FileType;
import io.dataease.commons.exception.DEException;
import io.dataease.plugins.common.base.domain.FileContent;
import io.dataease.plugins.common.base.domain.FileContentExample;
import io.dataease.plugins.common.base.domain.FileMetadata;
import io.dataease.plugins.common.base.domain.FileMetadataExample;
import io.dataease.plugins.common.base.mapper.FileContentMapper;
import io.dataease.plugins.common.base.mapper.FileMetadataMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private FileContentMapper fileContentMapper;

    public byte[] loadFileAsBytes(String id) {
        FileContent fileContent = fileContentMapper.selectByPrimaryKey(id);
        return fileContent.getFile();
    }

    public FileContent getFileContent(String fileId) {
        return fileContentMapper.selectByPrimaryKey(fileId);
    }

    public void deleteFileByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andIdIn(ids);
        fileMetadataMapper.deleteByExample(example);
        FileContentExample example2 = new FileContentExample();
        example2.createCriteria().andFileIdIn(ids);
        fileContentMapper.deleteByExample(example2);
    }

    public FileMetadata saveFile(MultipartFile file) {
        return saveFile(file, file.getOriginalFilename());
    }

    public FileMetadata saveFile(MultipartFile file, String fileName) {
        final FileMetadata fileMetadata = new FileMetadata();
        setFileMetadataProperties(fileMetadata, file.getSize(), fileName);
        fileMetadataMapper.insert(fileMetadata);
        FileContent fileContent = new FileContent();
        fileContent.setFileId(fileMetadata.getId());
        try {
            fileContent.setFile(file.getBytes());
        } catch (IOException e) {
            DEException.throwException(e);
        }
        fileContentMapper.insert(fileContent);

        return fileMetadata;
    }

    private void setFileMetadataProperties(FileMetadata fileMetadata, long size, String fileName){
        fileMetadata.setId(UUID.randomUUID().toString());
        fileMetadata.setName(fileName);
        fileMetadata.setSize(size);
        fileMetadata.setCreateTime(System.currentTimeMillis());
        fileMetadata.setUpdateTime(System.currentTimeMillis());
        FileType fileType = getFileType(fileMetadata.getName());
        fileMetadata.setType(fileType.name());
    }

    public FileMetadata saveFile(byte[] fileByte, String fileName, Long fileSize) {
        final FileMetadata fileMetadata = new FileMetadata();
        setFileMetadataProperties(fileMetadata, fileSize, fileName);
        fileMetadataMapper.insert(fileMetadata);
        FileContent fileContent = new FileContent();
        fileContent.setFileId(fileMetadata.getId());
        fileContent.setFile(fileByte);
        fileContentMapper.insert(fileContent);
        return fileMetadata;
    }

    public FileMetadata copyFile(String fileId) {
        FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(fileId);
        FileContent fileContent = getFileContent(fileId);
        if (fileMetadata != null && fileContent != null) {
            fileMetadata.setId(UUID.randomUUID().toString());
            fileMetadata.setCreateTime(System.currentTimeMillis());
            fileMetadata.setUpdateTime(System.currentTimeMillis());
            fileMetadataMapper.insert(fileMetadata);

            fileContent.setFileId(fileMetadata.getId());
            fileContentMapper.insert(fileContent);
        }
        return fileMetadata;
    }

    private FileType getFileType(String filename) {
        int s = filename.lastIndexOf(".") + 1;
        String type = filename.substring(s);
        return FileType.valueOf(type.toUpperCase());
    }

    public void deleteFileById(String fileId) {
        deleteFileByIds(Collections.singletonList(fileId));
    }

    public FileMetadata getFileMetadataById(String fileId) {
        return fileMetadataMapper.selectByPrimaryKey(fileId);
    }
}
