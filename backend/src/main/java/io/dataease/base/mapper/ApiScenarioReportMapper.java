package io.dataease.base.mapper;

import io.dataease.base.domain.ApiScenarioReport;
import io.dataease.base.domain.ApiScenarioReportExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiScenarioReportMapper {
    long countByExample(ApiScenarioReportExample example);

    int deleteByExample(ApiScenarioReportExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiScenarioReport record);

    int insertSelective(ApiScenarioReport record);

    List<ApiScenarioReport> selectByExampleWithBLOBs(ApiScenarioReportExample example);

    List<ApiScenarioReport> selectByExample(ApiScenarioReportExample example);

    ApiScenarioReport selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiScenarioReport record, @Param("example") ApiScenarioReportExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiScenarioReport record, @Param("example") ApiScenarioReportExample example);

    int updateByExample(@Param("record") ApiScenarioReport record, @Param("example") ApiScenarioReportExample example);

    int updateByPrimaryKeySelective(ApiScenarioReport record);

    int updateByPrimaryKeyWithBLOBs(ApiScenarioReport record);

    int updateByPrimaryKey(ApiScenarioReport record);
}
