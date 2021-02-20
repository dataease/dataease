package io.dataease.base.mapper;

import io.dataease.base.domain.TestPlanReportData;
import io.dataease.base.domain.TestPlanReportDataExample;
import io.dataease.base.domain.TestPlanReportDataWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestPlanReportDataMapper {
    long countByExample(TestPlanReportDataExample example);

    int deleteByExample(TestPlanReportDataExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanReportDataWithBLOBs record);

    int insertSelective(TestPlanReportDataWithBLOBs record);

    List<TestPlanReportDataWithBLOBs> selectByExampleWithBLOBs(TestPlanReportDataExample example);

    List<TestPlanReportData> selectByExample(TestPlanReportDataExample example);

    TestPlanReportDataWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanReportDataWithBLOBs record, @Param("example") TestPlanReportDataExample example);

    int updateByExampleWithBLOBs(@Param("record") TestPlanReportDataWithBLOBs record, @Param("example") TestPlanReportDataExample example);

    int updateByExample(@Param("record") TestPlanReportData record, @Param("example") TestPlanReportDataExample example);

    int updateByPrimaryKeySelective(TestPlanReportDataWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(TestPlanReportDataWithBLOBs record);

    int updateByPrimaryKey(TestPlanReportData record);
}
