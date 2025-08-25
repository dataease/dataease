package io.dataease.exportCenter.dao.auto.mapper;

import io.dataease.exportCenter.dao.auto.entity.CoreExportDownloadTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * <p>
 * 下载任务列表 Mapper 接口
 * </p>
 *
 * @author fit2cloud
 * @since 2025-06-16
 */

public interface CoreExportDownloadTaskRepository extends JpaRepository<CoreExportDownloadTask, String>, JpaSpecificationExecutor<CoreExportDownloadTask> {

}
