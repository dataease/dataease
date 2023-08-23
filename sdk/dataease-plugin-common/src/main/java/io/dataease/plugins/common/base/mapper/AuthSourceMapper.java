package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.AuthSourceExample;
import java.util.List;

import io.dataease.plugins.common.base.domain.AuthSource;
import org.apache.ibatis.annotations.Param;

public interface AuthSourceMapper {
    long countByExample(AuthSourceExample example);

    int deleteByExample(AuthSourceExample example);

    int deleteByPrimaryKey(String id);

    int insert(AuthSource record);

    int insertSelective(AuthSource record);

    List<AuthSource> selectByExampleWithBLOBs(AuthSourceExample example);

    List<AuthSource> selectByExample(AuthSourceExample example);

    AuthSource selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") AuthSource record, @Param("example") AuthSourceExample example);

    int updateByExampleWithBLOBs(@Param("record") AuthSource record, @Param("example") AuthSourceExample example);

    int updateByExample(@Param("record") AuthSource record, @Param("example") AuthSourceExample example);

    int updateByPrimaryKeySelective(AuthSource record);

    int updateByPrimaryKeyWithBLOBs(AuthSource record);

    int updateByPrimaryKey(AuthSource record);
}
