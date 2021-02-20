package io.dataease.service;

import io.dataease.dto.TestResourcePoolDTO;

public interface KubernetesResourcePoolService {
    boolean validate(TestResourcePoolDTO testResourcePool);
}
