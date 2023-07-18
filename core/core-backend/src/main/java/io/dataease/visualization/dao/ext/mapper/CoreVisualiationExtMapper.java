package io.dataease.visualization.dao.ext.mapper;

import io.dataease.visualization.dao.ext.po.VisualizationNodePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CoreVisualiationExtMapper {

    @Select("select id, name, pid, node_type from data_visualization_info where type = #{type}")
    List<VisualizationNodePO> queryNodes(@Param("type") String type);
}
