package io.dataease.ext;

import io.dataease.mobile.dto.MeItemDTO;

public interface MobileMeMapper {

    MeItemDTO query(Long userId);
}
