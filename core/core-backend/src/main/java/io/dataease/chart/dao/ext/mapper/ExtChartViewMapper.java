package io.dataease.chart.dao.ext.mapper;

import io.dataease.api.chart.vo.ViewSelectorVO;
import io.dataease.chart.dao.auto.entity.CoreChartView;
import io.dataease.chart.dao.ext.entity.ChartBasePO;
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
}
