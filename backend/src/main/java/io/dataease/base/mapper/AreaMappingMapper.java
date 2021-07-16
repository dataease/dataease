package io.dataease.base.mapper;

import io.dataease.base.domain.AreaMapping;
import io.dataease.base.domain.AreaMappingExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AreaMappingMapper {
    long countByExample(AreaMappingExample example);

    int deleteByExample(AreaMappingExample example);

    int deleteByPrimaryKey(Long id);

    int insert(AreaMapping record);

    int insertSelective(AreaMapping record);

    List<AreaMapping> selectByExample(AreaMappingExample example);

    AreaMapping selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") AreaMapping record, @Param("example") AreaMappingExample example);

    int updateByExample(@Param("record") AreaMapping record, @Param("example") AreaMappingExample example);

    int updateByPrimaryKeySelective(AreaMapping record);

    int updateByPrimaryKey(AreaMapping record);
}