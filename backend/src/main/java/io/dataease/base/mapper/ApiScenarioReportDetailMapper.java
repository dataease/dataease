package io.dataease.base.mapper;

import io.dataease.base.domain.ApiScenarioReportDetail;
import io.dataease.base.domain.ApiScenarioReportDetailExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiScenarioReportDetailMapper {
    long countByExample(ApiScenarioReportDetailExample example);

    int deleteByExample(ApiScenarioReportDetailExample example);

    int deleteByPrimaryKey(String reportId);

    int insert(ApiScenarioReportDetail record);

    int insertSelective(ApiScenarioReportDetail record);

    List<ApiScenarioReportDetail> selectByExampleWithBLOBs(ApiScenarioReportDetailExample example);

    List<ApiScenarioReportDetail> selectByExample(ApiScenarioReportDetailExample example);

    ApiScenarioReportDetail selectByPrimaryKey(String reportId);

    int updateByExampleSelective(@Param("record") ApiScenarioReportDetail record, @Param("example") ApiScenarioReportDetailExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiScenarioReportDetail record, @Param("example") ApiScenarioReportDetailExample example);

    int updateByExample(@Param("record") ApiScenarioReportDetail record, @Param("example") ApiScenarioReportDetailExample example);

    int updateByPrimaryKeySelective(ApiScenarioReportDetail record);

    int updateByPrimaryKeyWithBLOBs(ApiScenarioReportDetail record);

    int updateByPrimaryKey(ApiScenarioReportDetail record);
}
