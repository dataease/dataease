package io.dataease.base.mapper;

import io.dataease.base.domain.LoadTestReportResult;
import io.dataease.base.domain.LoadTestReportResultExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LoadTestReportResultMapper {
    long countByExample(LoadTestReportResultExample example);

    int deleteByExample(LoadTestReportResultExample example);

    int deleteByPrimaryKey(String id);

    int insert(LoadTestReportResult record);

    int insertSelective(LoadTestReportResult record);

    List<LoadTestReportResult> selectByExampleWithBLOBs(LoadTestReportResultExample example);

    List<LoadTestReportResult> selectByExample(LoadTestReportResultExample example);

    LoadTestReportResult selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") LoadTestReportResult record, @Param("example") LoadTestReportResultExample example);

    int updateByExampleWithBLOBs(@Param("record") LoadTestReportResult record, @Param("example") LoadTestReportResultExample example);

    int updateByExample(@Param("record") LoadTestReportResult record, @Param("example") LoadTestReportResultExample example);

    int updateByPrimaryKeySelective(LoadTestReportResult record);

    int updateByPrimaryKeyWithBLOBs(LoadTestReportResult record);

    int updateByPrimaryKey(LoadTestReportResult record);
}
