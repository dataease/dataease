package io.datains.base.mapper.ext;

import io.datains.mobile.dto.HomeItemDTO;
import io.datains.mobile.dto.HomeItemShareDTO;

import java.util.List;
import java.util.Map;

public interface HomeMapper {

    List<HomeItemDTO> queryStore(Map<String, Object> param);

    List<HomeItemDTO> queryHistory();

    List<HomeItemShareDTO> queryShare(Map<String, Object> param);
}
