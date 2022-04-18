package io.datains.base.mapper.ext;

import io.datains.mobile.dto.MeItemDTO;

public interface MobileMeMapper {

    MeItemDTO query(Long userId);
}
