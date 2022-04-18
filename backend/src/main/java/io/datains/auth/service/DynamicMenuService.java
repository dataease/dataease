package io.datains.auth.service;

import io.datains.auth.api.dto.DynamicMenuDto;

import java.util.List;

public interface DynamicMenuService {

    List<DynamicMenuDto> load(String userId);
}
