package io.dataease.chart.dao.ext.mapper;

import io.dataease.api.chart.vo.ViewSelectorVO;
import io.dataease.chart.dao.auto.entity.CoreChartView;
import io.dataease.chart.dao.ext.entity.ChartBasePO;
import io.dataease.extensions.view.dto.ChartViewDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExtChartViewMapper {

    @Select("""
            select id, scene_id as pid, title, type from core_chart_view where type != 'VQuery' and scene_id = #{resourceId}
            """)
    List<ViewSelectorVO> queryViewOption(@Param("resourceId") Long resourceId);

    ChartBasePO queryChart(@Param("id") Long id, @Param("resourceTable")String resourceTable);

    List<CoreChartView> selectListCustom(@Param("sceneId") Long sceneId, @Param("resourceTable") String resourceTable);

    void deleteViewsBySceneId(@Param("sceneId") Long sceneId, @Param("resourceTable") String resourceTable);

    @Select("""
            SELECT id, scene_id as pid, title, type FROM (
                SELECT id, scene_id, title, type FROM core_chart_view 
                WHERE id = #{viewId}
                UNION ALL
                SELECT id, scene_id, title, type FROM snapshot_core_chart_view 
                WHERE id = #{viewId} 
            ) combined_views
            LIMIT 1
            """)
    ChartViewDTO findChartViewAround(@Param("viewId") String viewId);
}
