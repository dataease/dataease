package io.datains.base.mapper.ext;

import io.datains.controller.request.authModel.VAuthModelRequest;
import io.datains.dto.authModel.VAuthModelDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtVAuthModelMapper {

    List<VAuthModelDTO> queryAuthModel(@Param("record") VAuthModelRequest record);

    List<VAuthModelDTO> queryAuthModelViews (@Param("record") VAuthModelRequest record);

    List<VAuthModelDTO> queryAuthViewsOriginal (@Param("record") VAuthModelRequest record);
}
