package io.dataease.base.mapper;

import io.dataease.base.domain.LoadTestReportDetail;
import io.dataease.base.domain.LoadTestReportDetailExample;
import io.dataease.base.domain.LoadTestReportDetailKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LoadTestReportDetailMapper {
    long countByExample(LoadTestReportDetailExample example);

    int deleteByExample(LoadTestReportDetailExample example);

    int deleteByPrimaryKey(LoadTestReportDetailKey key);

    int insert(LoadTestReportDetail record);

    int insertSelective(LoadTestReportDetail record);

    List<LoadTestReportDetail> selectByExampleWithBLOBs(LoadTestReportDetailExample example);

    List<LoadTestReportDetail> selectByExample(LoadTestReportDetailExample example);

    LoadTestReportDetail selectByPrimaryKey(LoadTestReportDetailKey key);

    int updateByExampleSelective(@Param("record") LoadTestReportDetail record, @Param("example") LoadTestReportDetailExample example);

    int updateByExampleWithBLOBs(@Param("record") LoadTestReportDetail record, @Param("example") LoadTestReportDetailExample example);

    int updateByExample(@Param("record") LoadTestReportDetail record, @Param("example") LoadTestReportDetailExample example);

    int updateByPrimaryKeySelective(LoadTestReportDetail record);

    int updateByPrimaryKeyWithBLOBs(LoadTestReportDetail record);
}
