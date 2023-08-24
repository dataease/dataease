package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.SysBackgroundImage;
import io.dataease.plugins.common.base.domain.SysBackgroundImageExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SysBackgroundImageMapper {
    long countByExample(SysBackgroundImageExample example);

    int deleteByExample(SysBackgroundImageExample example);

    int deleteByPrimaryKey(String id);

    int insert(SysBackgroundImage record);

    int insertSelective(SysBackgroundImage record);

    List<SysBackgroundImage> selectByExampleWithBLOBs(SysBackgroundImageExample example);

    List<SysBackgroundImage> selectByExample(SysBackgroundImageExample example);

    SysBackgroundImage selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") SysBackgroundImage record, @Param("example") SysBackgroundImageExample example);

    int updateByExampleWithBLOBs(@Param("record") SysBackgroundImage record, @Param("example") SysBackgroundImageExample example);

    int updateByExample(@Param("record") SysBackgroundImage record, @Param("example") SysBackgroundImageExample example);

    int updateByPrimaryKeySelective(SysBackgroundImage record);

    int updateByPrimaryKeyWithBLOBs(SysBackgroundImage record);

    int updateByPrimaryKey(SysBackgroundImage record);
}