package io.dataease.visualization.ext;

import io.dataease.api.visualization.vo.DataVisualizationBaseVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExtDataVisualizationMapper {


    @Select("SELECT id, `name`, `name` as label, pid, org_id, node_type, mobile_layout, create_time, create_by, update_time, update_by \n" +
            "FROM\n" +
            "\tdata_visualization_info")
    List<DataVisualizationBaseVO> findBashInfo();

}
