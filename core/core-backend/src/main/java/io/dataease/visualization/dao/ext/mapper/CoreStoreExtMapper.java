package io.dataease.visualization.dao.ext.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.dataease.visualization.dao.ext.po.StorePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CoreStoreExtMapper {

    @Select("""
            select
            s.id as store_id,
            v.id as resource_id,
            v.type,
            v.create_by as creator,
            v.update_by as editor,
            v.update_time as edit_time,
            v.name
            from core_store s
            left join data_visualization_info v on s.resource_id = v.id
            ${ew.customSqlSegment}
            """)
    IPage<StorePO> query(IPage<StorePO> page, @Param("ew") QueryWrapper<Object> ew);
}
