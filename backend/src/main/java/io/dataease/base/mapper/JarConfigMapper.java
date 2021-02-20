package io.dataease.base.mapper;

import io.dataease.base.domain.JarConfig;
import io.dataease.base.domain.JarConfigExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface JarConfigMapper {
    long countByExample(JarConfigExample example);

    int deleteByExample(JarConfigExample example);

    int deleteByPrimaryKey(String id);

    int insert(JarConfig record);

    int insertSelective(JarConfig record);

    List<JarConfig> selectByExample(JarConfigExample example);

    JarConfig selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") JarConfig record, @Param("example") JarConfigExample example);

    int updateByExample(@Param("record") JarConfig record, @Param("example") JarConfigExample example);

    int updateByPrimaryKeySelective(JarConfig record);

    int updateByPrimaryKey(JarConfig record);
}
