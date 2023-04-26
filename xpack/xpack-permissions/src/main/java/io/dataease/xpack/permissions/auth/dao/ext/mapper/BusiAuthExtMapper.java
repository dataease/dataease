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
            id = "batchPermisionOriginMap",
            value = {
                    @Result(property = "id", column = "rid"),
                    @Result(property = "permissions", many = @Many(resultMap = "originMap"))
            }
    )
    List<PermissionOrigin> batchRolePermission(@Param("rids") List<Long> rids, @Param("rt") Integer rt);


}
