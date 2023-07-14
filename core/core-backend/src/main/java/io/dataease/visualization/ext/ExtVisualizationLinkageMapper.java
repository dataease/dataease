package io.dataease.visualization.ext;

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

    List<VisualizationLinkageDTO> getViewLinkageGather(@Param("dvId") String dvId, @Param("sourceViewId") String sourceViewId, @Param("targetViewIds") List<String> targetViewIds);

    List<LinkageInfoDTO> getPanelAllLinkageInfo(@Param("dvId") String dvId);

    List<DatasetTableFieldDTO> queryTableField(@Param("table_id") String tableId);

    List<DatasetTableFieldDTO> queryTableFieldWithViewId(@Param("viewId") String viewId);

    void deleteViewLinkage(@Param("dvId") String dvId,@Param("sourceViewId") String sourceViewId);

    void deleteViewLinkageField(@Param("dvId") String dvId,@Param("sourceViewId") String sourceViewId);

    void copyViewLinkage(@Param("copyId") String copyId);

    void copyViewLinkageField(@Param("copyId") String copyId);

    List<VisualizationLinkage> findLinkageWithDvId(@Param("dvId") String dvId);

    List<VisualizationLinkageField> findLinkageFieldWithDvId(@Param("dvId") String dvId);
}
