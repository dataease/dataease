package io.dataease.base.mapper;

import io.dataease.base.domain.LoadTestReport;
import io.dataease.base.domain.LoadTestReportExample;
import io.dataease.base.domain.LoadTestReportWithBLOBs;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LoadTestReportMapper {
    long countByExample(LoadTestReportExample example);

    int deleteByExample(LoadTestReportExample example);

    int deleteByPrimaryKey(String id);

    int insert(LoadTestReportWithBLOBs record);

    int insertSelective(LoadTestReportWithBLOBs record);

    List<LoadTestReportWithBLOBs> selectByExampleWithBLOBs(LoadTestReportExample example);

    List<LoadTestReport> selectByExample(LoadTestReportExample example);

    LoadTestReportWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") LoadTestReportWithBLOBs record, @Param("example") LoadTestReportExample example);

    int updateByExampleWithBLOBs(@Param("record") LoadTestReportWithBLOBs record, @Param("example") LoadTestReportExample example);

    int updateByExample(@Param("record") LoadTestReport record, @Param("example") LoadTestReportExample example);

    int updateByPrimaryKeySelective(LoadTestReportWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(LoadTestReportWithBLOBs record);

    int updateByPrimaryKey(LoadTestReport record);
}
