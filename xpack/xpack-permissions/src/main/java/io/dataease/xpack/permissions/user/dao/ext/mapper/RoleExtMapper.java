package io.dataease.xpack.permissions.user.dao.ext.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.dataease.xpack.permissions.user.dao.auto.entity.PerRole;
import io.dataease.xpack.permissions.user.dao.ext.entity.RolePO;
import io.dataease.xpack.permissions.user.entity.RoleInfo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleExtMapper extends BaseMapper<RolePO> {

    @Select("""
            select pr.id, pr.name
            from per_role pr 
            where NOT EXISTS( 
                select 1 from per_user_role pur where pur.uid = #{uid} and pur.rid = pr.id
            )
            ${ew.customSqlSegment} 
            """)
    List<RolePO> selectOptionForUser(@Param("uid") Long uid, @Param("ew") QueryWrapper queryWrapper);

    @Select("select id, name from per_role ${ew.customSqlSegment} ")
    List<RolePO> selectOptionForOrg(@Param("ew") QueryWrapper queryWrapper);

    @Select("""
            select pr.id, pr.name 
            from per_role pr 
            left join per_user_role pur on pr.id = pur.rid 
            ${ew.customSqlSegment} 
            """)
    List<RolePO> selectSelectedForUser(@Param("ew") QueryWrapper queryWrapper);

    @Delete("delete from per_user_role pur where EXISTS (select 1 from per_role pr where pr.org_id = #{oid} and pur.rid = pr.id)")
    void deleteUserRoleWithOrg(@Param("oid") Long oid);

    @Delete("delete from per_user_role pur where EXISTS (select 1 from per_role pr where pr.org_id = #{oid} and pur.rid = pr.id) and pur.uid = #{uid}")
    void deleteUserRole(@Param("oid") Long oid, @Param("uid") Long uid);

    @Select("select id, readonly, pid from per_role where id = #{rid}")
    PerRole selectRoleInfo(@Param("rid") Long rid);

    @Select("select pr.id, pr.name, pr.readonly, pr.pid from per_user_role pur left join per_role pr on pur.rid = pr.id where pur.uid = #{uid} and pur.oid = #{oid} ")
    List<PerRole> roleInfoByUid(@Param("uid") Long uid, @Param("oid") Long oid);

    @Select("select id from per_role where org_id = #{oid} and pid = 0 and readonly = 0")
    Long adminRoleId(@Param("oid") Long oid);

    @Select("select id from per_role where org_id = #{oid} and pid = 0 and readonly = 1")
    Long readonlyRoleId(@Param("oid") Long oid);
}
