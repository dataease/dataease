package io.dataease.ext;

import io.dataease.ext.query.GridExample;
import io.dataease.plugins.common.base.domain.MyPlugin;

import java.util.List;

public interface ExtSysPluginMapper {

    List<MyPlugin> query(GridExample example);
}
