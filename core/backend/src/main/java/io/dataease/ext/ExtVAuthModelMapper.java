package io.dataease.ext;

import io.dataease.controller.request.authModel.VAuthModelRequest;
import io.dataease.dto.authModel.VAuthModelDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtVAuthModelMapper {

    List<VAuthModelDTO> queryAuthModel(@Param("record") VAuthModelRequest record);

    List<VAuthModelDTO> queryAuthModelByIds(@Param("userId") String userId, @Param("modelType") String modelType, @Param("ids") List<String> ids);

    List<VAuthModelDTO> queryAuthModelViews(@Param("record") VAuthModelRequest record);

    List<VAuthModelDTO> queryAuthViewsOriginal(@Param("record") VAuthModelRequest record);
}
