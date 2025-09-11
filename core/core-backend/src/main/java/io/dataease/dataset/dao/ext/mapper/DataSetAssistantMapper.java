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
            cd.id as cd_id,
            cd.name as cd_name,
            cd.description as cd_description,
            cd.type as cd_type,
            cd.configuration as cd_configuration,
            
            cdg.id as cdg_id, 
            cdg.name as cdg_name,
            cdg.type as cdg_type,
            cdg.mode as cdg_model,
            cdg.info as cdg_info,
            cdg.union_sql as cdg_union_sql,
            cdg.is_cross as cdg_is_cross,
            
            cdt.id as cdt_id,
            cdt.table_name as cdt_table_name,
            cdt.type as cdt_type,
            cdt.info as cdt_info,
            cdt.sql_variable_details as cdt_sql_variable_details,
            
            cdtf.id as cdtf_id,
            cdtf.origin_name as cdtf_origin_name,
            cdtf.name as cdtf_name,
            cdtf.description as cdtf_description,
            cdtf.dataease_name as cdtf_dataease_name,
            cdtf.field_short_name as cdtf_field_short_name,
            cdtf.group_list as cdtf_group_list,
            cdtf.other_group as cdtf_other_group,
            cdtf.group_type as cdtf_group_type,
            cdtf.type as cdtf_type,
            cdtf.de_type as cdtf_de_type,
            cdtf.de_extract_type as cdtf_de_extract_type,
            cdtf.ext_field as cdtf_ext_field,
            cdtf.checked as cdtf_checked,
            cdtf.accuracy as cdtf_accuracy,
            cdtf.date_format as cdtf_date_format,
            cdtf.date_format_type as cdtf_date_format_type,
            cdtf.params as cdtf_params
            
            from `core_dataset_group` cdg
            left join `core_dataset_table` cdt on cdg.id =  cdt.dataset_group_id
            left join `core_dataset_table_field` cdtf on cdtf.dataset_group_id = cdg.id
            inner join `core_datasource` cd on cdt.datasource_id = cd.id
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
        cd.id as cd_id,
        cd.name as cd_name,
        cd.description as cd_description,
        cd.type as cd_type,
        cd.configuration as cd_configuration,
        
        cdg.id as cdg_id, 
        cdg.name as cdg_name,
        cdg.type as cdg_type,
        cdg.mode as cdg_model,
        cdg.info as cdg_info,
        cdg.union_sql as cdg_union_sql,
        cdg.is_cross as cdg_is_cross,
        
        cdt.id as cdt_id,
        cdt.table_name as cdt_table_name,
        cdt.type as cdt_type,
        cdt.info as cdt_info,
        cdt.sql_variable_details as cdt_sql_variable_details,
        
        cdtf.id as cdtf_id,
        cdtf.origin_name as cdtf_origin_name,
        cdtf.name as cdtf_name,
        cdtf.description as cdtf_description,
        cdtf.dataease_name as cdtf_dataease_name,
        cdtf.field_short_name as cdtf_field_short_name,
        cdtf.group_list as cdtf_group_list,
        cdtf.other_group as cdtf_other_group,
        cdtf.group_type as cdtf_group_type,
        cdtf.type as cdtf_type,
        cdtf.de_type as cdtf_de_type,
        cdtf.de_extract_type as cdtf_de_extract_type,
        cdtf.ext_field as cdtf_ext_field,
        cdtf.checked as cdtf_checked,
        cdtf.accuracy as cdtf_accuracy,
        cdtf.date_format as cdtf_date_format,
        cdtf.date_format_type as cdtf_date_format_type,
        cdtf.params as cdtf_params
    FROM `core_dataset_table` cdt
    INNER JOIN `core_datasource` cd ON cdt.datasource_id = cd.id and (cd.STATUS IS NULL OR cd.STATUS != 'Error')
    INNER JOIN `core_dataset_group` cdg ON cdg.id = cdt.dataset_group_id
        AND cdg.is_cross != 1
    INNER JOIN `core_dataset_table_field` cdtf ON cdtf.dataset_group_id = cdg.id
    where not exists( select 1 from user_ds_permissions ds_p where cd.id = ds_p.resource_id )
    and not exists( select 1 from user_dg_permissions dg_p where cdg.id = dg_p.resource_id )
    ${ew.customSqlSegment}
    """)
    List<Map<String, Object>> queryCommunity(@Param("ew") QueryWrapper queryWrapper);



    @Select("""
    <script>
    WITH user_ds_permissions AS (
        <choose>
            <when test="orgAdmin">
                SELECT id as resource_id FROM per_busi_resource 
                WHERE org_id = #{oid} AND rt_id = 4
            </when>
            <otherwise>
                SELECT DISTINCT resource_id
                FROM (
                    SELECT resource_id FROM per_auth_busi_user
                    WHERE oid = #{oid} AND uid = #{uid} AND resource_type = 4
                    UNION ALL
                    SELECT a.resource_id FROM per_auth_busi_role a
                    INNER JOIN per_user_role b ON a.rid = b.rid
                    WHERE b.oid = #{oid} AND b.uid = #{uid} AND a.resource_type = 4
                ) temp
            </otherwise>
        </choose>
    ),
    user_dg_permissions AS (
        <choose>
            <when test="orgAdmin">
                SELECT id as resource_id FROM per_busi_resource 
                WHERE org_id = #{oid} AND rt_id = 3
            </when>
            <otherwise>
                SELECT DISTINCT resource_id
                FROM (
                    SELECT resource_id FROM per_auth_busi_user
                    WHERE oid = #{oid} AND uid = #{uid} AND resource_type = 3
                    UNION ALL
                    SELECT a.resource_id FROM per_auth_busi_role a
                    INNER JOIN per_user_role b ON a.rid = b.rid
                    WHERE b.oid = #{oid} AND b.uid = #{uid} AND a.resource_type = 3
                ) temp
            </otherwise>
        </choose>
    )
    SELECT
        cd.id as cd_id,
        cd.name as cd_name,
        cd.description as cd_description,
        cd.type as cd_type,
        cd.configuration as cd_configuration,
        
        cdg.id as cdg_id, 
        cdg.name as cdg_name,
        cdg.type as cdg_type,
        cdg.mode as cdg_model,
        cdg.info as cdg_info,
        cdg.union_sql as cdg_union_sql,
        cdg.is_cross as cdg_is_cross,
        
        cdt.id as cdt_id,
        cdt.table_name as cdt_table_name,
        cdt.type as cdt_type,
        cdt.info as cdt_info,
        cdt.sql_variable_details as cdt_sql_variable_details,
        
        cdtf.id as cdtf_id,
        cdtf.origin_name as cdtf_origin_name,
        cdtf.name as cdtf_name,
        cdtf.description as cdtf_description,
        cdtf.dataease_name as cdtf_dataease_name,
        cdtf.field_short_name as cdtf_field_short_name,
        cdtf.group_list as cdtf_group_list,
        cdtf.other_group as cdtf_other_group,
        cdtf.group_type as cdtf_group_type,
        cdtf.type as cdtf_type,
        cdtf.de_type as cdtf_de_type,
        cdtf.de_extract_type as cdtf_de_extract_type,
        cdtf.ext_field as cdtf_ext_field,
        cdtf.checked as cdtf_checked,
        cdtf.accuracy as cdtf_accuracy,
        cdtf.date_format as cdtf_date_format,
        cdtf.date_format_type as cdtf_date_format_type,
        cdtf.params as cdtf_params
    FROM `core_dataset_table` cdt
    INNER JOIN `core_datasource` cd ON cdt.datasource_id = cd.id 
        AND (cd.STATUS IS NULL OR cd.STATUS != 'Error')
    INNER JOIN `core_dataset_group` cdg ON cdg.id = cdt.dataset_group_id
        AND cdg.is_cross != 1
    INNER JOIN `core_dataset_table_field` cdtf ON cdtf.dataset_group_id = cdg.id
    INNER JOIN user_ds_permissions ds_p ON cd.id = ds_p.resource_id
    INNER JOIN user_dg_permissions dg_p ON cdg.id = dg_p.resource_id
    ${ew.customSqlSegment}
    </script>
""")
    List<Map<String, Object>> queryEnterprise(@Param("oid") Long oid, @Param("uid") Long uid, @Param("orgAdmin") boolean orgAdmin, @Param("ew") QueryWrapper queryWrapper);


    @Select("select pr.id, pr.readonly, pr.pid from per_user_role pur left join per_role pr on pur.rid = pr.id where pur.uid = #{uid} and pur.oid = #{oid} ")
    List<Map<String, Object>> roleInfoByUid(@Param("uid") Long uid, @Param("oid") Long oid);
}
