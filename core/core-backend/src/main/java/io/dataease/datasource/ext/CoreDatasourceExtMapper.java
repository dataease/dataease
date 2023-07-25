package io.dataease.datasource.ext;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.datasource.dto.DatasourceNodePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CoreDatasourceExtMapper {

    @Select("""
            select id, name, type, pid from core_datasource
            ${ew.customSqlSegment}
            """)
    List<DatasourceNodePO> query(@Param("ew") QueryWrapper queryWrapper);

}
