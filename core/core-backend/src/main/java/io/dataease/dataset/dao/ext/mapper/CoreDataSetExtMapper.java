package io.dataease.dataset.dao.ext.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.dataset.vo.DataSetBarVO;
import io.dataease.dataset.dao.ext.po.DataSetNodePO;
import io.dataease.model.BusiNodeRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CoreDataSetExtMapper {

    @Select("""
            select id, name, node_type, pid from core_dataset_group
            ${ew.customSqlSegment}
            """)
    List<DataSetNodePO> query(@Param("ew") QueryWrapper queryWrapper);

    @Select("select id, name, node_type, create_by, create_time from core_dataset_group where id = #{id}")
    DataSetBarVO queryBarInfo(@Param("id") Long id);
}
