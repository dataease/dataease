package io.datains.base.mapper;

import io.datains.base.domain.FilePicture;

import java.util.List;

/**
 * <p>
 * 图片表 Mapper 接口
 * </p>
 *
 * @author Mr.zhang
 * @since 2022-07-11
 */
public interface FilePictureMapper{

    boolean insert(FilePicture filePicture);

    List<FilePicture> getList(Integer type);
}
