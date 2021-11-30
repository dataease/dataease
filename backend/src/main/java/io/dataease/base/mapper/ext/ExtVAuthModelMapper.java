package io.dataease.base.mapper.ext;

import io.dataease.controller.request.authModel.VAuthModelRequest;
import io.dataease.dto.authModel.VAuthModelDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtVAuthModelMapper {

    List<VAuthModelDTO> queryAuthModel (@Param("record") VAuthModelRequest record);

}
