package io.dataease.extensions.datasource.api;

import io.dataease.extensions.datasource.vo.XpackPluginsDatasourceVO;

import java.util.List;

/**
 * @Author Junjun
 */
public interface PluginManageApi {
    List<XpackPluginsDatasourceVO> queryPluginDs();
}
