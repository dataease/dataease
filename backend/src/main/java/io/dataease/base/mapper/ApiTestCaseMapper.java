package io.dataease.base.mapper;

import io.dataease.base.domain.ApiTestCase;
import io.dataease.base.domain.ApiTestCaseExample;
import io.dataease.base.domain.ApiTestCaseWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiTestCaseMapper {
    long countByExample(ApiTestCaseExample example);

    int deleteByExample(ApiTestCaseExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiTestCaseWithBLOBs record);

    int insertSelective(ApiTestCaseWithBLOBs record);

    List<ApiTestCaseWithBLOBs> selectByExampleWithBLOBs(ApiTestCaseExample example);

    List<ApiTestCase> selectByExample(ApiTestCaseExample example);

    ApiTestCaseWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiTestCaseWithBLOBs record, @Param("example") ApiTestCaseExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiTestCaseWithBLOBs record, @Param("example") ApiTestCaseExample example);

    int updateByExample(@Param("record") ApiTestCase record, @Param("example") ApiTestCaseExample example);

    int updateByPrimaryKeySelective(ApiTestCaseWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(ApiTestCaseWithBLOBs record);

    int updateByPrimaryKey(ApiTestCase record);
}
