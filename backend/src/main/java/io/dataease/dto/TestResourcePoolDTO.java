package io.dataease.dto;

import io.dataease.base.domain.TestResource;
import io.dataease.base.domain.TestResourcePool;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestResourcePoolDTO extends TestResourcePool {

    private List<TestResource> resources;

}
