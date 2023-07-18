package io.dataease.xpack.permissions.auth.dao.ext.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.permissions.auth.dto.PermissionBO;
import io.dataease.api.permissions.auth.vo.PermissionItem;
import io.dataease.api.permissions.auth.vo.PermissionOrigin;
import io.dataease.xpack.permissions.auth.dao.ext.entity.BusiResourcePO;
import io.dataease.xpack.permissions.auth.dao.ext.entity.ResourcePO;
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

    @Select("select distinct id, name, pid, leaf, extra_flag from per_busi_resource ${ew.customSqlSegment} ")
    List<BusiResourcePO> query(@Param("ew") QueryWrapper queryWrapper);

    @Select("select root_way from per_busi_resource where id = #{id}")
    String queryRootWay(@Param("id") Long id);

    @Select("select resource_id as id, weight from per_auth_busi_role where rid = #{rid} and resource_type = #{rt}")
    List<PermissionItem> rolePermission(@Param("rid") Long rid, @Param("rt") Integer rt);

    @Select("select pabu.resource_id as id, pabu.weight from per_auth_busi_user pabu where uid = #{uid} and resource_type = #{rt} and oid = #{oid}")
    List<PermissionItem> userPermission(@Param("uid") Long uid, @Param("rt") Integer rt, @Param("oid") Long oid);

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

    @Select("select uid as id, weight from per_auth_busi_user where resource_id = #{resourceId} and resource_type = #{resourceType} and oid = #{oid}")
    List<PermissionItem> busiUserPermission(@Param("resourceId") Long resourceId, @Param("resourceType") Integer resourceType, @Param("oid") Long oid);


    @Select("""
            select distinct pabr.rid as id, pr.name, pur.uid, pabr.weight
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

    @Select("select pr.id, pr.name, pur.uid, 9 as weight from per_user_role pur left join per_role pr on pr.id = pur.rid where pr.org_id = #{oid} and pr.pid = 0 and pr.readonly = 0")
    @Results(id = "adminOriginMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "permissions", many = @Many(resultMap = "originUserMap"))
    })
    PermissionOrigin adminOrigin(@Param("oid") Long oid);

    @Select("select pr.id, pr.name, pur.uid, 1 as weight from per_user_role pur left join per_role pr on pr.id = pur.rid where pr.org_id = #{oid} and pr.pid = 0 and pr.readonly = 1")
    @Results(id = "readonlyOriginMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "permissions", many = @Many(resultMap = "originUserMap"))
    })
    PermissionOrigin readonlyOrigin(@Param("oid") Long oid);

    @Select("""
            <script>
            select resource_id, uid as id, weight from per_auth_busi_user where resource_type = #{resourceType} and resource_id in 
            
            <foreach item='resourceId' index='index' collection='resourceIds' open='(' separator=',' close=')'>
            #{resourceId}
            </foreach>
            
            and uid in 
            
            <foreach item='uid' index='index' collection='uids' open='(' separator=',' close=')'>
            #{uid}
            </foreach>           
            </script>
            """)
    List<PermissionBO> queryExistUserPer(@Param("resourceIds") List<Long> resourceIds, @Param("resourceType") Integer resourceType, @Param("uids") List<Long> uids);


    @Select("""
            <script>
            select resource_id, rid as id, weight from per_auth_busi_role where resource_type = #{resourceType} and resource_id in 
            
            <foreach item='resourceId' index='index' collection='resourceIds' open='(' separator=',' close=')'>
            #{resourceId}
            </foreach>
            
            and rid in 
            
            <foreach item='rid' index='index' collection='rids' open='(' separator=',' close=')'>
            #{rid}
            </foreach>           
            </script>
            """)
    List<PermissionBO> queryExistRolePer(@Param("resourceIds") List<Long> resourceIds, @Param("resourceType") Integer resourceType, @Param("rids") List<Long> rids);


    @Select("""
            select distinct pbr.id, pbr.name, pbr.pid 
            from 
            per_busi_resource pbr 
            left join per_auth_busi_user pabu on  pbr.id = pabu.resource_id
            ${ew.customSqlSegment} 
            """)
    List<BusiResourcePO> resourceByUid(@Param("ew") QueryWrapper queryWrapper);

    @Select("""
            select distinct pbr.id, pbr.name, pbr.pid 
            from 
            per_auth_busi_role pabr          
            left join per_busi_resource pbr on  pbr.id = pabr.resource_id
            ${ew.customSqlSegment} 
            """)
    List<BusiResourcePO> resourceByRid(@Param("ew") QueryWrapper queryWrapper);

    @Select("select rt_id type, id from per_busi_resource")
    List<ResourcePO> resourcePOs();

    @Select("select id from per_busi_resource where rt_id = #{rtid} and org_id = #{oid}")
    List<Long> resourceIdsByRt(@Param("rtid") Integer rtid, @Param("oid") Long oid);


    @Delete("""
            <script>
            delete from per_auth_busi_user where resource_id in
            <foreach item='resourceId' index='index' collection='ids' open='(' separator=',' close=')'>
            #{resourceId}
            </foreach>
            </script>
            """)
    int delUserPerByResource(@Param("ids") List<Long> ids);

    @Delete("""
            <script>
            delete from per_auth_busi_role where resource_id in
            <foreach item='resourceId' index='index' collection='ids' open='(' separator=',' close=')'>
            #{resourceId}
            </foreach>
            </script>
            """)
    int delRolePerByResource(@Param("ids") List<Long> ids);

    @Select("select weight from per_auth_busi_user where uid = #{uid} and resource_id = 0 and resource_type = #{rtid} and oid = #{oid}")
    Integer userRootWeight(@Param("uid") Long uid, @Param("rtid") Integer rtid, @Param("oid") Long oid);

    @Select("""
            <script>
            select rid as id, weight from per_auth_busi_role where resource_id = 0 and resource_type = #{rtid} and rid in 
            <foreach item='rid' index='index' collection='rids' open='(' separator=',' close=')'>
            #{rid}
            </foreach>
            </script>
            """)
    List<PermissionItem> roleRootWeight(@Param("rids") List<Long> rids, @Param("rtid") Integer rtid);

    @Select("select id from per_busi_resource where root_way like concat('%', #{id}, '%')")
    List<Long> childrenResourceIds(@Param("id") Long id);
}
