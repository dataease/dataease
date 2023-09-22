package io.dataease.visualization.dao.ext.mapper;

import io.dataease.api.visualization.dto.LinkageInfoDTO;
import io.dataease.api.visualization.dto.VisualizationLinkageDTO;
import io.dataease.dto.dataset.DatasetTableFieldDTO;
import io.dataease.visualization.dao.auto.entity.VisualizationLinkage;
import io.dataease.visualization.dao.auto.entity.VisualizationLinkageField;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExtVisualizationLinkageMapper {

    List<VisualizationLinkageDTO> getViewLinkageGather(@Param("dvId") Long dvId, @Param("sourceViewId") Long sourceViewId, @Param("targetViewIds") List<String> targetViewIds);

    List<LinkageInfoDTO> getPanelAllLinkageInfo(@Param("dvId") Long dvId);

    List<DatasetTableFieldDTO> queryTableField(@Param("table_id") Long tableId);

    List<DatasetTableFieldDTO> queryTableFieldWithViewId(@Param("viewId") Long viewId);

    void deleteViewLinkage(@Param("dvId") Long dvId,@Param("sourceViewId") Long sourceViewId);

    void deleteViewLinkageField(@Param("dvId") Long dvId,@Param("sourceViewId") Long sourceViewId);

    void copyViewLinkage(@Param("copyId") Long copyId);

    void copyViewLinkageField(@Param("copyId") Long copyId);

    List<VisualizationLinkage> findLinkageWithDvId(@Param("dvId") Long dvId);

    List<VisualizationLinkageField> findLinkageFieldWithDvId(@Param("dvId") Long dvId);
}
