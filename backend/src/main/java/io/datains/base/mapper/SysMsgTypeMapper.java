package io.datains.base.mapper;

import io.datains.base.domain.SysMsgType;
import io.datains.base.domain.SysMsgTypeExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SysMsgTypeMapper {
    long countByExample(SysMsgTypeExample example);

    int deleteByExample(SysMsgTypeExample example);

    int deleteByPrimaryKey(Long msgTypeId);

    int insert(SysMsgType record);

    int insertSelective(SysMsgType record);

    List<SysMsgType> selectByExample(SysMsgTypeExample example);

    SysMsgType selectByPrimaryKey(Long msgTypeId);

    int updateByExampleSelective(@Param("record") SysMsgType record, @Param("example") SysMsgTypeExample example);

    int updateByExample(@Param("record") SysMsgType record, @Param("example") SysMsgTypeExample example);

    int updateByPrimaryKeySelective(SysMsgType record);

    int updateByPrimaryKey(SysMsgType record);
}