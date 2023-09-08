package io.dataease.ext;

import org.apache.ibatis.annotations.Param;


public interface ExtSysAuthMapper {

    Boolean checkTreeNoManageCount(@Param("userId") Long userId, @Param("modelType") String modelType, @Param("nodeId") String nodeId);

    String copyAuth(@Param("authSource") String authSource, @Param("authSourceType") String authSourceType, @Param("authUser") String authUser);

}
