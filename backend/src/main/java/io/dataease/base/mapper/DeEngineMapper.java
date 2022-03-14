package io.dataease.base.mapper;

import io.dataease.base.domain.DeEngine;
import io.dataease.base.domain.DeEngineExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DeEngineMapper {
    long countByExample(DeEngineExample example);

    int deleteByExample(DeEngineExample example);

    int deleteByPrimaryKey(String id);

    int insert(DeEngine record);

    int insertSelective(DeEngine record);

    List<DeEngine> selectByExampleWithBLOBs(DeEngineExample example);

    List<DeEngine> selectByExample(DeEngineExample example);

    DeEngine selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") DeEngine record, @Param("example") DeEngineExample example);

    int updateByExampleWithBLOBs(@Param("record") DeEngine record, @Param("example") DeEngineExample example);

    int updateByExample(@Param("record") DeEngine record, @Param("example") DeEngineExample example);

    int updateByPrimaryKeySelective(DeEngine record);

    int updateByPrimaryKeyWithBLOBs(DeEngine record);

    int updateByPrimaryKey(DeEngine record);
}