package io.dataease.extensions.sync.api;

import io.dataease.extensions.sync.vo.XpackPluginsSyncDatasourceVO;

import java.util.List;

/**
 * @author jianneng
 */
public interface SyncPluginManageApi {
    List<XpackPluginsSyncDatasourceVO> queryPluginSyncDatasource();
}
