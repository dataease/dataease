package io.dataease.base.mapper.ext;

import io.dataease.controller.request.BaseTreeRequest;
import io.dataease.dto.VAuthModelDTO;

import java.util.List;

public interface ExtVAuthModelMapper {
    List<VAuthModelDTO> searchTree(BaseTreeRequest request);

}
