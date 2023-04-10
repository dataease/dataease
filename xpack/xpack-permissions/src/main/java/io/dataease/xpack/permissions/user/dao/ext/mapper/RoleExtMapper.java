package io.dataease.xpack.permissions.user.dao.ext.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.dataease.xpack.permissions.user.dao.ext.entity.RolePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleExtMapper extends BaseMapper<RolePO> {

    @Select("""
            select pr.id, pr.name
            from per_role pr 
            where not exist( 
                select 1 from per_user_role pur where pur.uid = #{uid} and pur.rid = pr.id
            )
            ${ew.customSqlSegment} 
            """)
    List<RolePO> selectOptionForUser(@Param("uid") Long uid, @Param("ew") QueryWrapper queryWrapper);

}
