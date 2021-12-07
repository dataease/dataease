package io.dataease.base.mapper.ext;

import io.dataease.mobile.dto.HomeItemDTO;
import java.util.List;
import java.util.Map;

public interface HomeMapper {


    List<HomeItemDTO> queryStore(Long userId);

    List<HomeItemDTO> queryHistory();

    List<HomeItemDTO> queryShare(Map<String, Object> param);
}
