package io.dataease.visualization.dao.auto.mapper;

import io.dataease.visualization.dao.auto.entity.VisualizationReportFilter;
import java.util.List;

public interface VisualizationReportFilterRepository {

    List<VisualizationReportFilter> findByResourceIdAndTaskId( Long resourceId, Long taskId);
}
