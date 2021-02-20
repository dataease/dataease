package io.dataease.base.mapper;

import io.dataease.base.domain.SwaggerUrlProject;
import io.dataease.base.domain.SwaggerUrlProjectExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SwaggerUrlProjectMapper {
    long countByExample(SwaggerUrlProjectExample example);

    int deleteByExample(SwaggerUrlProjectExample example);

    int deleteByPrimaryKey(String id);

    int insert(SwaggerUrlProject record);

    int insertSelective(SwaggerUrlProject record);

    List<SwaggerUrlProject> selectByExample(SwaggerUrlProjectExample example);

    SwaggerUrlProject selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") SwaggerUrlProject record, @Param("example") SwaggerUrlProjectExample example);

    int updateByExample(@Param("record") SwaggerUrlProject record, @Param("example") SwaggerUrlProjectExample example);

    int updateByPrimaryKeySelective(SwaggerUrlProject record);

    int updateByPrimaryKey(SwaggerUrlProject record);
}
