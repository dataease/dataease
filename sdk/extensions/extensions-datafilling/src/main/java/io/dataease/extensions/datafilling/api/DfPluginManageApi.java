package io.dataease.extensions.datafilling.api;

import io.dataease.extensions.datafilling.vo.XpackPluginsDfVO;

import java.util.List;

public interface DfPluginManageApi {
    List<XpackPluginsDfVO> queryPluginDf();
    XpackPluginsDfVO queryPluginDf(String type);
}
