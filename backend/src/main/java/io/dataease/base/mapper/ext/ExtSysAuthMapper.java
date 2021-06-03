package io.dataease.base.mapper.ext;

import io.dataease.controller.request.SysAuthRequest;
import io.dataease.dto.SysAuthDetailDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtSysAuthMapper {
    List<SysAuthDetailDTO> searchAuth(SysAuthRequest request);

    Boolean authExist(@Param("authSource") String authSource, @Param("authTarget") String authTarget);

    String findAuthId(@Param("authSource") String authSource,
                      @Param("authSourceType") String authSourceType,
                      @Param("authTarget") String authTarget,
                      @Param("authTargetType") String authTargetType);


}
