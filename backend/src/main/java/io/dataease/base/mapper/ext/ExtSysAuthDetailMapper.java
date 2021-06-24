package io.dataease.base.mapper.ext;

import io.dataease.base.domain.SysAuthDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtSysAuthDetailMapper {
    List<SysAuthDetail> searchAuthTypeModel(@Param("authTypeModel") String authTypeModel);

    void copyAuthModel(@Param("authTypeModel") String authTypeModel,@Param("authId") String authId,@Param("createUser") String createUser);

    void authDetailsChange(@Param("privilegeValue")Integer privilegeValue,@Param("privilegeType")Integer privilegeType,@Param("authIds")List<String> authIds);
}
