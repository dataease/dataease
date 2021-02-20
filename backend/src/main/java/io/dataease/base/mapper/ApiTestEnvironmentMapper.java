package io.dataease.base.mapper;

import io.dataease.base.domain.ApiTestEnvironment;
import io.dataease.base.domain.ApiTestEnvironmentExample;
import io.dataease.base.domain.ApiTestEnvironmentWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiTestEnvironmentMapper {
    long countByExample(ApiTestEnvironmentExample example);

    int deleteByExample(ApiTestEnvironmentExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiTestEnvironmentWithBLOBs record);

    int insertSelective(ApiTestEnvironmentWithBLOBs record);

    List<ApiTestEnvironmentWithBLOBs> selectByExampleWithBLOBs(ApiTestEnvironmentExample example);

    List<ApiTestEnvironment> selectByExample(ApiTestEnvironmentExample example);

    ApiTestEnvironmentWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiTestEnvironmentWithBLOBs record, @Param("example") ApiTestEnvironmentExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiTestEnvironmentWithBLOBs record, @Param("example") ApiTestEnvironmentExample example);

    int updateByExample(@Param("record") ApiTestEnvironment record, @Param("example") ApiTestEnvironmentExample example);

    int updateByPrimaryKeySelective(ApiTestEnvironmentWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(ApiTestEnvironmentWithBLOBs record);

    int updateByPrimaryKey(ApiTestEnvironment record);
}
