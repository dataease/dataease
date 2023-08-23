package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.SysDictItem;
import io.dataease.plugins.common.base.domain.SysDictItemExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SysDictItemMapper {
    long countByExample(SysDictItemExample example);

    int deleteByExample(SysDictItemExample example);

    int deleteByPrimaryKey(String id);

    int insert(SysDictItem record);

    int insertSelective(SysDictItem record);

    List<SysDictItem> selectByExample(SysDictItemExample example);

    SysDictItem selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") SysDictItem record, @Param("example") SysDictItemExample example);

    int updateByExample(@Param("record") SysDictItem record, @Param("example") SysDictItemExample example);

    int updateByPrimaryKeySelective(SysDictItem record);

    int updateByPrimaryKey(SysDictItem record);
}