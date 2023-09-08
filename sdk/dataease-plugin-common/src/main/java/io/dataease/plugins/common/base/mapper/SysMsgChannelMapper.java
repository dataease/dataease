package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.SysMsgChannel;
import io.dataease.plugins.common.base.domain.SysMsgChannelExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SysMsgChannelMapper {
    long countByExample(SysMsgChannelExample example);

    int deleteByExample(SysMsgChannelExample example);

    int deleteByPrimaryKey(Long msgChannelId);

    int insert(SysMsgChannel record);

    int insertSelective(SysMsgChannel record);

    List<SysMsgChannel> selectByExample(SysMsgChannelExample example);

    SysMsgChannel selectByPrimaryKey(Long msgChannelId);

    int updateByExampleSelective(@Param("record") SysMsgChannel record, @Param("example") SysMsgChannelExample example);

    int updateByExample(@Param("record") SysMsgChannel record, @Param("example") SysMsgChannelExample example);

    int updateByPrimaryKeySelective(SysMsgChannel record);

    int updateByPrimaryKey(SysMsgChannel record);
}