package io.dataease.base.mapper;

import io.dataease.base.domain.ApiTestReportDetail;
import io.dataease.base.domain.ApiTestReportDetailExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiTestReportDetailMapper {
    long countByExample(ApiTestReportDetailExample example);

    int deleteByExample(ApiTestReportDetailExample example);

    int deleteByPrimaryKey(String reportId);

    int insert(ApiTestReportDetail record);

    int insertSelective(ApiTestReportDetail record);

    List<ApiTestReportDetail> selectByExampleWithBLOBs(ApiTestReportDetailExample example);

    List<ApiTestReportDetail> selectByExample(ApiTestReportDetailExample example);

    ApiTestReportDetail selectByPrimaryKey(String reportId);

    int updateByExampleSelective(@Param("record") ApiTestReportDetail record, @Param("example") ApiTestReportDetailExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiTestReportDetail record, @Param("example") ApiTestReportDetailExample example);

    int updateByExample(@Param("record") ApiTestReportDetail record, @Param("example") ApiTestReportDetailExample example);

    int updateByPrimaryKeySelective(ApiTestReportDetail record);

    int updateByPrimaryKeyWithBLOBs(ApiTestReportDetail record);

    int updateByPrimaryKey(ApiTestReportDetail record);
}
