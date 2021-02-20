package io.dataease.base.mapper;

import io.dataease.base.domain.TestCaseReportTemplate;
import io.dataease.base.domain.TestCaseReportTemplateExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestCaseReportTemplateMapper {
    long countByExample(TestCaseReportTemplateExample example);

    int deleteByExample(TestCaseReportTemplateExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestCaseReportTemplate record);

    int insertSelective(TestCaseReportTemplate record);

    List<TestCaseReportTemplate> selectByExampleWithBLOBs(TestCaseReportTemplateExample example);

    List<TestCaseReportTemplate> selectByExample(TestCaseReportTemplateExample example);

    TestCaseReportTemplate selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestCaseReportTemplate record, @Param("example") TestCaseReportTemplateExample example);

    int updateByExampleWithBLOBs(@Param("record") TestCaseReportTemplate record, @Param("example") TestCaseReportTemplateExample example);

    int updateByExample(@Param("record") TestCaseReportTemplate record, @Param("example") TestCaseReportTemplateExample example);

    int updateByPrimaryKeySelective(TestCaseReportTemplate record);

    int updateByPrimaryKeyWithBLOBs(TestCaseReportTemplate record);

    int updateByPrimaryKey(TestCaseReportTemplate record);
}
