package io.dataease.base.mapper.ext;

import io.dataease.base.domain.MyPlugin;
import io.dataease.base.mapper.ext.query.GridExample;

import java.util.List;

public interface ExtSysPluginMapper {

    List<MyPlugin> query(GridExample example);
}
