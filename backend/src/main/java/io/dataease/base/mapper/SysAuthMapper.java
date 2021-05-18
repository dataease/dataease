package io.dataease.base.mapper;

import io.dataease.base.domain.SysAuth;
import io.dataease.base.domain.SysAuthExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SysAuthMapper {
    long countByExample(SysAuthExample example);

    int deleteByExample(SysAuthExample example);

    int deleteByPrimaryKey(String id);

    int insert(SysAuth record);

    int insertSelective(SysAuth record);

    List<SysAuth> selectByExample(SysAuthExample example);

    SysAuth selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") SysAuth record, @Param("example") SysAuthExample example);

    int updateByExample(@Param("record") SysAuth record, @Param("example") SysAuthExample example);

    int updateByPrimaryKeySelective(SysAuth record);

    int updateByPrimaryKey(SysAuth record);
}