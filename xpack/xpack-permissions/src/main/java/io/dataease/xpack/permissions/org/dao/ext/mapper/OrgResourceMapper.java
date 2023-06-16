package io.dataease.xpack.permissions.org.dao.ext.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrgResourceMapper {

    @Select("select uid from per_user_role where oid = #{oid}")
    List<Long> uids(@Param("oid") Long oid);

    @Select("select id from per_role where org_id = #{oid}")
    List<Long> rids(@Param("oid") Long oid);

    @Select("select id from per_busi_resource where org_id = #{oid} and rt_id = 1")
    List<Long> panelIds(@Param("oid") Long oid);

    @Select("select id from per_busi_resource where org_id = #{oid} and rt_id = 2")
    List<Long> screenIds(@Param("oid") Long oid);

    @Select("select id from per_busi_resource where org_id = #{oid} and rt_id = 3")
    List<Long> datasetIds(@Param("oid") Long oid);

    @Select("select id from per_busi_resource where org_id = #{oid} and rt_id = 4")
    List<Long> datasourceIds(@Param("oid") Long oid);

    @Select("select id from per_menu_resource")
    List<Long> menuIds();
}
