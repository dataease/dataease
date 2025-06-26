package io.dataease.dao.auto.repo;

import io.dataease.dao.auto.entity.VisualizationReportFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author fit2cloud
 * @since 2024-06-26
 */
public interface VisualizationReportFilterRepository extends JpaRepository<VisualizationReportFilter, Long>, JpaSpecificationExecutor<VisualizationReportFilter> {

    void deleteByReportId(Long reportId);

    List<VisualizationReportFilter> findByResourceIdAndTaskId(Long resourceId, Long taskId);

}
