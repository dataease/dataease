package io.dataease.base.mapper;

import io.dataease.base.domain.SysAuthDetail;
import io.dataease.base.domain.SysAuthDetailExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SysAuthDetailMapper {
    long countByExample(SysAuthDetailExample example);

    int deleteByExample(SysAuthDetailExample example);

    int deleteByPrimaryKey(String id);

    int insert(SysAuthDetail record);

    int insertSelective(SysAuthDetail record);

    List<SysAuthDetail> selectByExample(SysAuthDetailExample example);

    SysAuthDetail selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") SysAuthDetail record, @Param("example") SysAuthDetailExample example);

    int updateByExample(@Param("record") SysAuthDetail record, @Param("example") SysAuthDetailExample example);

    int updateByPrimaryKeySelective(SysAuthDetail record);

    int updateByPrimaryKey(SysAuthDetail record);
}