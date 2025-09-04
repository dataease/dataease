package io.dataease.dataset.dao.ext.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DataSetAssistantMapper {

    @Select(
    """
            select
            cdt.id, cdt.datasource_id, cdt.table_name, cdt.info,
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
            where  cdg.is_cross != 1 and (cd.STATUS IS NULL OR cd.STATUS != 'Error')
            ${ew.customSqlSegment}
            """
    )
    List<Map<String, Object>> queryAll(@Param("ew") QueryWrapper queryWrapper);

    @Select("""
    WITH user_ds_permissions AS (
        SELECT DISTINCT resource_id
        FROM (
            select id as resource_id from per_busi_resource where rt_id = 4 
        ) temp
    ),
    user_dg_permissions AS (
        SELECT DISTINCT resource_id
        FROM (
            select id as resource_id from per_busi_resource where rt_id = 3
        ) temp
    )
    SELECT
        cdt.id, cdt.datasource_id, cdt.table_name, cdt.info,
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
    FROM `core_dataset_table` cdt
    INNER JOIN `core_datasource` cd ON cdt.datasource_id = cd.id and (cd.STATUS IS NULL OR cd.STATUS != 'Error')
    INNER JOIN `core_dataset_group` cdg ON cdg.id = cdt.dataset_group_id
        AND cdg.is_cross != 1
    INNER JOIN `core_dataset_table_field` cdtf ON cdtf.dataset_table_id = cdt.id
    where not exists( select 1 from user_ds_permissions ds_p where cd.id = ds_p.resource_id )
    and not exists( select 1 from user_dg_permissions dg_p where cdg.id = dg_p.resource_id )
    ${ew.customSqlSegment}
    """)
    List<Map<String, Object>> queryCommunity(@Param("ew") QueryWrapper queryWrapper);



    @Select("""
    <script>
    WITH user_ds_permissions AS (
        <choose>
            <when test="!orgAdmin">
                SELECT DISTINCT resource_id
                FROM (
                    SELECT resource_id FROM per_auth_busi_user
                    WHERE oid = #{oid} AND uid = #{uid} AND resource_type = 4
                    UNION ALL
                    SELECT a.resource_id FROM per_auth_busi_role a
                    INNER JOIN per_user_role b ON a.rid = b.rid
                    WHERE b.oid = #{oid} AND b.uid = #{uid} AND a.resource_type = 4
                ) temp
            </when>
            <otherwise>
                SELECT id as resource_id FROM per_busi_resource 
                WHERE org_id = #{oid} AND rt_id = 4
            </otherwise>
        </choose>
    ),
    user_dg_permissions AS (
        <choose>
            <when test="!orgAdmin">
                SELECT DISTINCT resource_id
                FROM (
                    SELECT resource_id FROM per_auth_busi_user
                    WHERE oid = #{oid} AND uid = #{uid} AND resource_type = 3
                    UNION ALL
                    SELECT a.resource_id FROM per_auth_busi_role a
                    INNER JOIN per_user_role b ON a.rid = b.rid
                    WHERE b.oid = #{oid} AND b.uid = #{uid} AND a.resource_type = 3
                ) temp
            </when>
            <otherwise>
                SELECT id as resource_id FROM per_busi_resource 
                WHERE org_id = #{oid} AND rt_id = 3
            </otherwise>
        </choose>
    )
    SELECT
        cdt.id, cdt.datasource_id, cdt.table_name, cdt.info,
        cdg.id as dataset_group_id,
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
        cdtf.field_short_name,
        cdtf.group_type,
        cdtf.de_type,
        cdtf.de_extract_type,
        cdtf.ext_field,
        cdtf.date_format,
        cdtf.date_format_type,
        cdtf.params,
        cdtf.type as field_type
    FROM `core_dataset_table` cdt
    INNER JOIN `core_datasource` cd ON cdt.datasource_id = cd.id 
        AND (cd.STATUS IS NULL OR cd.STATUS != 'Error')
    INNER JOIN `core_dataset_group` cdg ON cdg.id = cdt.dataset_group_id
        AND cdg.is_cross != 1
    INNER JOIN `core_dataset_table_field` cdtf ON cdtf.dataset_table_id = cdt.id
    INNER JOIN user_ds_permissions ds_p ON cd.id = ds_p.resource_id
    INNER JOIN user_dg_permissions dg_p ON cdg.id = dg_p.resource_id
    ${ew.customSqlSegment}
    </script>
""")
    List<Map<String, Object>> queryEnterprise(@Param("oid") Long oid, @Param("uid") Long uid, @Param("orgAdmin") Boolean orgAdmin, @Param("ew") QueryWrapper queryWrapper);


}
