package io.dataease.base.mapper.ext;

import io.dataease.mobile.dto.MeItemDTO;

public interface MobileMeMapper {

    MeItemDTO query(Long userId);
}
