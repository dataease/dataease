package io.dataease.ext;


import io.dataease.plugins.common.base.domain.ChartViewField;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExtChartViewFieldMapper {
    List<ChartViewField> findByPanelId(@Param("panelId") String panelId);

}
