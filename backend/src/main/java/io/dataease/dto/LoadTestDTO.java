package io.dataease.dto;

import io.dataease.base.domain.LoadTest;
import io.dataease.base.domain.Schedule;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoadTestDTO extends LoadTest {
    private String projectName;
    private String userName;
    private Schedule schedule;
}
