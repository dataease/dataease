package io.dataease.auth.service;

import io.dataease.auth.api.dto.DynamicMenuDto;

import java.util.List;

public interface DynamicMenuService {

    List<DynamicMenuDto> load(String userId);
}
