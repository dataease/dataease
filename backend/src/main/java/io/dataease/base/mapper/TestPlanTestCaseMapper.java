package io.dataease.base.mapper;

import io.dataease.base.domain.TestPlanTestCase;
import io.dataease.base.domain.TestPlanTestCaseExample;
import io.dataease.base.domain.TestPlanTestCaseWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestPlanTestCaseMapper {
    long countByExample(TestPlanTestCaseExample example);

    int deleteByExample(TestPlanTestCaseExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanTestCaseWithBLOBs record);

    int insertSelective(TestPlanTestCaseWithBLOBs record);

    List<TestPlanTestCaseWithBLOBs> selectByExampleWithBLOBs(TestPlanTestCaseExample example);

    List<TestPlanTestCase> selectByExample(TestPlanTestCaseExample example);

    TestPlanTestCaseWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanTestCaseWithBLOBs record, @Param("example") TestPlanTestCaseExample example);

    int updateByExampleWithBLOBs(@Param("record") TestPlanTestCaseWithBLOBs record, @Param("example") TestPlanTestCaseExample example);

    int updateByExample(@Param("record") TestPlanTestCase record, @Param("example") TestPlanTestCaseExample example);

    int updateByPrimaryKeySelective(TestPlanTestCaseWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(TestPlanTestCaseWithBLOBs record);

    int updateByPrimaryKey(TestPlanTestCase record);
}
