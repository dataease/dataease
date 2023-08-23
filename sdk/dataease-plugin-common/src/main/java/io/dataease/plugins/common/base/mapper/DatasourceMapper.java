package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.Datasource;
import io.dataease.plugins.common.base.domain.DatasourceExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DatasourceMapper {
    long countByExample(DatasourceExample example);

    int deleteByExample(DatasourceExample example);

    int deleteByPrimaryKey(String id);

    int insert(Datasource record);

    int insertSelective(Datasource record);

    List<Datasource> selectByExampleWithBLOBs(DatasourceExample example);

    List<Datasource> selectByExample(DatasourceExample example);

    Datasource selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") Datasource record, @Param("example") DatasourceExample example);

    int updateByExampleWithBLOBs(@Param("record") Datasource record, @Param("example") DatasourceExample example);

    int updateByExample(@Param("record") Datasource record, @Param("example") DatasourceExample example);

    int updateByPrimaryKeySelective(Datasource record);

    int updateByPrimaryKeyWithBLOBs(Datasource record);

    int updateByPrimaryKey(Datasource record);
}