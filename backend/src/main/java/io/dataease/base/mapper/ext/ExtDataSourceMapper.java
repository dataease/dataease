package io.dataease.base.mapper.ext;

import io.dataease.base.domain.Datasource;
import io.dataease.base.mapper.ext.query.GridExample;

import java.util.List;

public interface ExtDataSourceMapper {

    List<Datasource> query(GridExample example);
}
