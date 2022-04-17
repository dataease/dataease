package io.dataease.ext;

import io.dataease.mobile.entity.PanelEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MobileDirMapper {
    List<PanelEntity> query(@Param("pid") String pid,@Param("userId") String userId);

    List<PanelEntity> queryWithName(@Param("name") String name,@Param("userId") String userId);

    List<String> idsWithUser(String userId);

    List<String> idsWithDept(String deptId);

    List<String> idsWithRoles(@Param("roleIds") List<String> roleIds);
}
