package io.dataease.dataset.dao.ext.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DataSetAssistantMapper {

    @Select(
    """
            select
            cdt.*,
            cdg.name as dataset_name,
            cd.name as ds_name,
            cd.description as ds_desc,
            cd.type as ds_type,
            cd.configuration as ds_config,
            
            cdtf.id as field_id,
            cdtf.origin_name,
            cdtf.name as field_show_name,
            cdtf.description as field_desc,
            cdtf.dataease_name,
            cdtf.type as field_type
            
            from `core_dataset_table` cdt
            left join `core_datasource` cd on  cdt.datasource_id = cd.id
            left join `core_dataset_table_field` cdtf on cdtf.dataset_table_id = cdt.id
            left join `core_dataset_group` cdg on cdg.id =  cdt.dataset_group_id
            where cdg.mode = 0 and cdg.is_cross != 1 and cd.status = 'Success'
            """
    )
    List<Map<String, Object>> query();
}
