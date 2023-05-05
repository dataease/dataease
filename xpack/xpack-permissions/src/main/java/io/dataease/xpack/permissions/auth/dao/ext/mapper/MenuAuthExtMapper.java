package io.dataease.xpack.permissions.auth.dao.ext.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
}
