package io.datains.base.mapper;

import io.datains.base.domain.FilePicture;
import org.apache.ibatis.annotations.Param;

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

    List<FilePicture> getList(@Param("type") Integer type,@Param("name") String name);

    List<FilePicture> getListByName();

    boolean del(Integer id);

    boolean delName(String name);

    List<FilePicture> getNull();

    boolean update(FilePicture filePicture);

    boolean updateName(@Param("oldName")String oldName,@Param("newName")String newName);
}
