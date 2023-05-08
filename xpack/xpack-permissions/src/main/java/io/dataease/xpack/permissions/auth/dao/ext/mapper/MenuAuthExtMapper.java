package io.dataease.xpack.permissions.auth.dao.ext.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.permissions.auth.dto.PermissionBO;
import io.dataease.api.permissions.auth.vo.PermissionItem;
import io.dataease.xpack.permissions.auth.dao.ext.entity.BusiResourcePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MenuAuthExtMapper {

    @Select("select id, name, pid from per_menu_resource ${ew.customSqlSegment} ")
    List<BusiResourcePO> query(@Param("ew") QueryWrapper queryWrapper);

    @Select("select resource_id as id, weight from per_auth_menu where rid = #{rid} ")
    List<PermissionItem> rolePermission(@Param("rid") Long rid);

    @Select("select rid as id, weight from per_auth_menu where resource_id = #{menuId}")
    List<PermissionItem> menuTargetPermission(@Param("menuId") Long menuId);

    @Select("""
            <script>
            select resource_id, rid as id, weight from per_auth_busi_role where resource_type = #{resourceType} and resource_id in 
            
            <foreach item='menuId' index='index' collection='menuIds' open='(' separator=',' close=')'>
            #{menuId}
            </foreach>
            
            and rid in 
            
            <foreach item='rid' index='index' collection='rids' open='(' separator=',' close=')'>
            #{rid}
            </foreach>           
            </script>
            """)
    List<PermissionBO> queryExistPer(@Param("menuIds") List<Long> menuIds, @Param("rids") List<Long> rids);

    @Select("""
            select distinct pmr.id, pmr.name, pmr.pid 
            from per_menu_resource pmr
            left join per_auth_menu pam on pam.resource_id = pmr.id
            ${ew.customSqlSegment} 
            """)
    List<BusiResourcePO> menusByRids(@Param("ew") QueryWrapper queryWrapper);

    @Select("select id from per_menu_resource")
    List<Long> menuIds();
}
