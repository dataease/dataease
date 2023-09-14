package io.dataease.ext;

import io.dataease.plugins.common.base.domain.MyPlugin;
import io.dataease.plugins.common.request.KeywordRequest;

import java.util.List;

public interface ExtSysPluginMapper {

    List<MyPlugin> query(KeywordRequest request);

    int updateVersion(String version);
}
