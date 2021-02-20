package io.dataease.base.mapper.ext;

import org.apache.ibatis.annotations.Param;

public interface ExtLoadTestReportDetailMapper {
    int appendLine(@Param("reportId") String id, @Param("line") String line);
}
