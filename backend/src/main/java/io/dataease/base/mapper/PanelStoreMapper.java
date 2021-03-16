package io.dataease.base.mapper;

import io.dataease.base.domain.PanelStore;
import io.dataease.base.domain.PanelStoreExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PanelStoreMapper {
    long countByExample(PanelStoreExample example);

    int deleteByExample(PanelStoreExample example);

    int deleteByPrimaryKey(Long storeId);

    int insert(PanelStore record);

    int insertSelective(PanelStore record);

    List<PanelStore> selectByExample(PanelStoreExample example);

    PanelStore selectByPrimaryKey(Long storeId);

    int updateByExampleSelective(@Param("record") PanelStore record, @Param("example") PanelStoreExample example);

    int updateByExample(@Param("record") PanelStore record, @Param("example") PanelStoreExample example);

    int updateByPrimaryKeySelective(PanelStore record);

    int updateByPrimaryKey(PanelStore record);
}