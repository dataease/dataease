package io.dataease.dataset.dao.ext.mapper;

import io.dataease.api.dataset.vo.DataSetBarVO;
import io.dataease.dataset.dao.ext.po.DataSetNodePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CoreDataSetExtMapper {

    @Select("select id, name, node_type, pid from core_dataset_group order by create_time desc")
    List<DataSetNodePO> query();

    @Select("select id, name, node_type, create_by, create_time from core_dataset_group where id = #{id}")
    DataSetBarVO queryBarInfo(@Param("id") Long id);
}
