package io.dataease.base.mapper.ext;

import io.dataease.mobile.entity.PanelEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MobileDirMapper {
    List<PanelEntity> query(String pid);

    List<PanelEntity> queryWithName(String name);

    List<String> idsWithUser(String userId);

    List<String> idsWithDept(String deptId);

    List<String> idsWithRoles(@Param("roleIds") List<String> roleIds);
}
