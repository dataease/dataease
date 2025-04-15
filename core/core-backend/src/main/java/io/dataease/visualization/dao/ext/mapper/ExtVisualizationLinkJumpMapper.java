package io.dataease.visualization.dao.ext.mapper;

import io.dataease.api.visualization.dto.VisualizationLinkJumpDTO;
import io.dataease.api.visualization.request.VisualizationLinkJumpBaseRequest;
import io.dataease.api.visualization.vo.VisualizationLinkJumpInfoVO;
import io.dataease.api.visualization.vo.VisualizationLinkJumpVO;
import io.dataease.api.visualization.vo.VisualizationOutParamsJumpVO;
import io.dataease.api.visualization.vo.VisualizationViewTableVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface ExtVisualizationLinkJumpMapper {
    List<VisualizationLinkJumpDTO> queryWithDvId(@Param("dvId") Long dvId,@Param("uid") Long uid,@Param("isDesktop") Boolean isDesktop);

    List<VisualizationLinkJumpDTO> queryWithDvIdSnapshot(@Param("dvId") Long dvId,@Param("uid") Long uid,@Param("isDesktop") Boolean isDesktop);

    VisualizationLinkJumpDTO queryWithViewId(@Param("dvId") Long dvId,@Param("viewId") Long viewId,@Param("uid") Long uid,@Param("isDesktop") Boolean isDesktop);

    void deleteJumpTargetViewInfoSnapshot(@Param("dvId") Long dvId,@Param("viewId") Long viewId);

    void deleteJumpInfoSnapshot(@Param("dvId") Long dvId,@Param("viewId") Long viewId);

    void deleteJumpSnapshot(@Param("dvId") Long dvId,@Param("viewId") Long viewId);

    void deleteJumpTargetViewInfoWithVisualization(@Param("dvId") Long dvId);

    void deleteJumpInfoWithVisualization(@Param("dvId") Long dvId);

    void deleteJumpWithVisualization(@Param("dvId") Long dvId);

    void deleteJumpTargetViewInfoWithVisualizationSnapshot(@Param("dvId") Long dvId);

    void deleteJumpInfoWithVisualizationSnapshot(@Param("dvId") Long dvId);

    void deleteJumpWithVisualizationSnapshot(@Param("dvId") Long dvId);

    List<VisualizationLinkJumpDTO> getTargetVisualizationJumpInfo(@Param("request") VisualizationLinkJumpBaseRequest request);

    List<VisualizationLinkJumpDTO> getTargetVisualizationJumpInfoSnapshot(@Param("request") VisualizationLinkJumpBaseRequest request);

    void copyLinkJump(@Param("copyId")Long copyId);

    void copyLinkJumpInfo(@Param("copyId")Long copyId);

    void copyLinkJumpTarget(@Param("copyId")Long copyId);

    List<VisualizationLinkJumpVO> findLinkJumpWithDvId(@Param("dvId")Long dvId);

    List<VisualizationLinkJumpInfoVO> findLinkJumpInfoWithDvId(@Param("dvId")Long dvId);

    List<VisualizationViewTableVO> getViewTableDetails(@Param("dvId")Long dvId);

    List<VisualizationOutParamsJumpVO> queryOutParamsTargetWithDvId(@Param("dvId")Long dvId);
}
