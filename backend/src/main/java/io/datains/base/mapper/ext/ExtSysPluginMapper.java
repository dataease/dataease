package io.datains.base.mapper.ext;

import io.datains.base.domain.MyPlugin;
import io.datains.base.mapper.ext.query.GridExample;

import java.util.List;

public interface ExtSysPluginMapper {

    List<MyPlugin> query(GridExample example);
}
