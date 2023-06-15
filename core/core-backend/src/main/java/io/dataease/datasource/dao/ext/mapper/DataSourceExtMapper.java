package io.dataease.datasource.dao.ext.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.dataease.datasource.dao.ext.po.DataSourceNodePO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DataSourceExtMapper extends BaseMapper<DataSourceNodePO> {
}
