package io.dataease.ext;

import io.dataease.plugins.common.base.domain.DatasetTableField;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtDataSetTableFieldMapper {
    List<DatasetTableField> findByPanelId(@Param("panelId") String panelId);

    List<DatasetTableField> findByTableIds(@Param("tableIds") List<String> tableIds);

}
