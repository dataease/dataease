package io.datains.service;

import io.datains.base.domain.FileContent;
import io.datains.base.domain.FileMetadata;
import io.datains.base.domain.FilePicture;
import io.datains.base.mapper.FilePictureMapper;
import io.datains.commons.exception.DEException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 图片表 服务实现类
 * </p>
 *
 * @author Mr.zhang
 * @since 2022-07-11
 */
@Service
public class FilePictureService {


    @Resource
    private FilePictureMapper filePictureMapper;

    public void save(FilePicture filePicture) {
        filePictureMapper.insert(filePicture);
    }

    public List<FilePicture> getList(Integer type){
        return filePictureMapper.getList(type);
    }
}
