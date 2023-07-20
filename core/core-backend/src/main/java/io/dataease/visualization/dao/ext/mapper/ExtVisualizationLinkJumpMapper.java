package io.dataease.visualization.ext;

import io.dataease.api.visualization.dto.VisualizationLinkJumpDTO;
import io.dataease.api.visualization.request.VisualizationLinkJumpBaseRequest;
import io.dataease.api.visualization.vo.VisualizationLinkJumpInfoVO;
import io.dataease.api.visualization.vo.VisualizationLinkJumpVO;
import io.dataease.api.visualization.vo.VisualizationViewTableVO;
import io.dataease.dto.dataset.DatasetTableFieldDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface ExtVisualizationLinkJumpMapper {
    List<VisualizationLinkJumpDTO> queryWithDvId(@Param("dvId") String dvId);

    VisualizationLinkJumpDTO queryWithViewId(@Param("dvId") String dvId,@Param("viewId") String viewId);

    void deleteJumpTargetViewInfo(@Param("dvId") String dvId,@Param("viewId") String viewId);

    void deleteJumpInfo(@Param("dvId") String dvId,@Param("viewId") String viewId);

    void deleteJump(@Param("dvId") String dvId,@Param("viewId") String viewId);

    void deleteJumpTargetViewInfoWithVisualization(@Param("dvId") String dvId);

    void deleteJumpInfoWithVisualization(@Param("dvId") String dvId);

    void deleteJumpWithVisualization(@Param("dvId") String dvId);

    List<VisualizationLinkJumpDTO> getTargetVisualizationJumpInfo(@Param("request") VisualizationLinkJumpBaseRequest request);

    void copyLinkJump(@Param("copyId")String copyId);

    void copyLinkJumpInfo(@Param("copyId")String copyId);

    void copyLinkJumpTarget(@Param("copyId")String copyId);

    List<VisualizationLinkJumpVO> findLinkJumpWithDvId(@Param("dvId")String dvId);
    List<VisualizationLinkJumpInfoVO> findLinkJumpInfoWithDvId(@Param("dvId")String dvId);
    List<VisualizationViewTableVO> getViewTableDetails(@Param("dvId")String dvId);
}
