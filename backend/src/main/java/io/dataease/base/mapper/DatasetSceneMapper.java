package io.dataease.base.mapper;

import io.dataease.base.domain.DatasetScene;
import io.dataease.base.domain.DatasetSceneExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DatasetSceneMapper {
    long countByExample(DatasetSceneExample example);

    int deleteByExample(DatasetSceneExample example);

    int deleteByPrimaryKey(String id);

    int insert(DatasetScene record);

    int insertSelective(DatasetScene record);

    List<DatasetScene> selectByExample(DatasetSceneExample example);

    DatasetScene selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") DatasetScene record, @Param("example") DatasetSceneExample example);

    int updateByExample(@Param("record") DatasetScene record, @Param("example") DatasetSceneExample example);

    int updateByPrimaryKeySelective(DatasetScene record);

    int updateByPrimaryKey(DatasetScene record);
}