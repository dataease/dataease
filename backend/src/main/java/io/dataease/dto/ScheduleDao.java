package io.dataease.dto;

import io.dataease.base.domain.Schedule;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduleDao extends Schedule {

    private String resourceName;
    private String userName;
}
