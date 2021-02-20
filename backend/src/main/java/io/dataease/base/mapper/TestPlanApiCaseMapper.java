package io.dataease.base.mapper;

import io.dataease.base.domain.TestPlanApiCase;
import io.dataease.base.domain.TestPlanApiCaseExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestPlanApiCaseMapper {
    long countByExample(TestPlanApiCaseExample example);

    int deleteByExample(TestPlanApiCaseExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanApiCase record);

    int insertSelective(TestPlanApiCase record);

    List<TestPlanApiCase> selectByExample(TestPlanApiCaseExample example);

    TestPlanApiCase selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanApiCase record, @Param("example") TestPlanApiCaseExample example);

    int updateByExample(@Param("record") TestPlanApiCase record, @Param("example") TestPlanApiCaseExample example);

    int updateByPrimaryKeySelective(TestPlanApiCase record);

    int updateByPrimaryKey(TestPlanApiCase record);
}
