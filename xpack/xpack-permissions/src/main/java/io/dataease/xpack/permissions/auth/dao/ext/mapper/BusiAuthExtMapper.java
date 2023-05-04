package io.dataease.xpack.permissions.auth.dao.ext.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.permissions.auth.vo.PermissionItem;
import io.dataease.api.permissions.auth.vo.PermissionOrigin;
import io.dataease.xpack.permissions.auth.dao.ext.entity.BusiResourcePO;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;

import java.util.List;

@Mapper
public interface BusiAuthExtMapper {

    @Results(
            id = "originMap", value = {
                    @Result(property = "id", column = "resource_id"),
                    @Result(property = "weight", column = "weight")
            }
    )
    @Select("select resource_id, weight from per_auth_busi_role where rid = #{rid}")
    List<PermissionItem> voidQuery(@Param("rid") Long rid);

    @Select("select id, name, pid from per_busi_resource ${ew.customSqlSegment} ")
    List<BusiResourcePO> query(@Param("ew") QueryWrapper queryWrapper);

    @Select("select root_way from per_busi_resource where id = #{id}")
    String queryRootWay(@Param("id") Long id);

    @Select("select resource_id as id, weight from per_auth_busi_role where rid = #{rid} and resource_type = #{rt}")
    List<PermissionItem> rolePermission(@Param("rid") Long rid, @Param("rt") Integer rt);

    @Select("select resource_id as id, weight from per_auth_busi_user where uid = #{uid} and resource_type = #{rt}")
    List<PermissionItem> userPermission(@Param("uid") Long uid, @Param("rt") Integer rt);

    @Select("""
            <script>
            select rid, resource_id, weight
            from per_auth_busi_role 
            where rid in 
            <foreach item='rid' index='index' collection='rids' open='(' separator=',' close=')'>
            #{rid}
            </foreach>
            and resource_type = #{rt}
            </script>
            """)
    @Results(
            id = "batchPermissionOriginMap",
            value = {
                    @Result(property = "id", column = "rid"),
                    @Result(property = "permissions", many = @Many(resultMap = "originMap"))
            }
    )
    List<PermissionOrigin> batchRolePermission(@Param("rids") List<Long> rids, @Param("rt") Integer rt);

    @Select("select rid as id, weight from per_auth_busi_role where resource_id = #{resourceId} and resource_type = #{resourceType}")
    List<PermissionItem> busiRolePermission(@Param("resourceId") Long resourceId, @Param("resourceType") Integer resourceType);

    @Select("select uid as id, weight from per_auth_busi_user where resource_id = #{resourceId} and resource_type = #{resourceType}")
    List<PermissionItem> busiUserPermission(@Param("resourceId") Long resourceId, @Param("resourceType") Integer resourceType);


    @Select("""
            select pabr.rid as id, pr.name, pur.uid, pabr.weight
            from per_auth_busi_role pabr
            left join per_role pr on pr.id = pabr.rid
            left join per_user_role pur on pur.rid = pr.id
            """)
    @Results(id = "batchUserPermissionMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "permissions", many = @Many(resultMap = "originUserMap"))
    })
    List<PermissionOrigin> batchUserRolePermission(@Param("resourceId") Long resourceId, @Param("resourceType") Integer resourceType);


    @Results(
            id = "originUserMap", value = {
            @Result(property = "id", column = "uid"),
            @Result(property = "weight", column = "weight")
    }
    )
    @Select("select uid, weight from per_auth_busi_user where resource_id = #{resourceId} and resource_type = #{resourceType}")
    List<PermissionItem> originVoidQuery(@Param("resourceId") Long resourceId, @Param("resourceType") Integer resourceType);
}
