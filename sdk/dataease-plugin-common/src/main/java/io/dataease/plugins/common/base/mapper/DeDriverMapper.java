package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.DeDriver;
import io.dataease.plugins.common.base.domain.DeDriverExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DeDriverMapper {
    long countByExample(DeDriverExample example);

    int deleteByExample(DeDriverExample example);

    int deleteByPrimaryKey(String id);

    int insert(DeDriver record);

    int insertSelective(DeDriver record);

    List<DeDriver> selectByExampleWithBLOBs(DeDriverExample example);

    List<DeDriver> selectByExample(DeDriverExample example);

    DeDriver selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") DeDriver record, @Param("example") DeDriverExample example);

    int updateByExampleWithBLOBs(@Param("record") DeDriver record, @Param("example") DeDriverExample example);

    int updateByExample(@Param("record") DeDriver record, @Param("example") DeDriverExample example);

    int updateByPrimaryKeySelective(DeDriver record);

    int updateByPrimaryKeyWithBLOBs(DeDriver record);

    int updateByPrimaryKey(DeDriver record);
}