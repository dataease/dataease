package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.AreaMappingGlobal;
import io.dataease.plugins.common.base.domain.AreaMappingGlobalExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AreaMappingGlobalMapper {
    long countByExample(AreaMappingGlobalExample example);

    int deleteByExample(AreaMappingGlobalExample example);

    int deleteByPrimaryKey(Long id);

    int insert(AreaMappingGlobal record);

    int insertSelective(AreaMappingGlobal record);

    List<AreaMappingGlobal> selectByExample(AreaMappingGlobalExample example);

    AreaMappingGlobal selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") AreaMappingGlobal record, @Param("example") AreaMappingGlobalExample example);

    int updateByExample(@Param("record") AreaMappingGlobal record, @Param("example") AreaMappingGlobalExample example);

    int updateByPrimaryKeySelective(AreaMappingGlobal record);

    int updateByPrimaryKey(AreaMappingGlobal record);
}