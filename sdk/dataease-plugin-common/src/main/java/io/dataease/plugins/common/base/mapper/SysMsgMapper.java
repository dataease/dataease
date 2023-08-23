package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.SysMsg;
import io.dataease.plugins.common.base.domain.SysMsgExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SysMsgMapper {
    long countByExample(SysMsgExample example);

    int deleteByExample(SysMsgExample example);

    int deleteByPrimaryKey(Long msgId);

    int insert(SysMsg record);

    int insertSelective(SysMsg record);

    List<SysMsg> selectByExample(SysMsgExample example);

    SysMsg selectByPrimaryKey(Long msgId);

    int updateByExampleSelective(@Param("record") SysMsg record, @Param("example") SysMsgExample example);

    int updateByExample(@Param("record") SysMsg record, @Param("example") SysMsgExample example);

    int updateByPrimaryKeySelective(SysMsg record);

    int updateByPrimaryKey(SysMsg record);
}