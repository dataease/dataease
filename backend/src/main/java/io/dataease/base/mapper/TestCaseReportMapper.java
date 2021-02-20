package io.dataease.base.mapper;

import io.dataease.base.domain.TestCaseReport;
import io.dataease.base.domain.TestCaseReportExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestCaseReportMapper {
    long countByExample(TestCaseReportExample example);

    int deleteByExample(TestCaseReportExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestCaseReport record);

    int insertSelective(TestCaseReport record);

    List<TestCaseReport> selectByExampleWithBLOBs(TestCaseReportExample example);

    List<TestCaseReport> selectByExample(TestCaseReportExample example);

    TestCaseReport selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestCaseReport record, @Param("example") TestCaseReportExample example);

    int updateByExampleWithBLOBs(@Param("record") TestCaseReport record, @Param("example") TestCaseReportExample example);

    int updateByExample(@Param("record") TestCaseReport record, @Param("example") TestCaseReportExample example);

    int updateByPrimaryKeySelective(TestCaseReport record);

    int updateByPrimaryKeyWithBLOBs(TestCaseReport record);

    int updateByPrimaryKey(TestCaseReport record);
}
