package io.datains.controller.request;

import io.datains.base.domain.Schedule;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class QueryScheduleRequest extends Schedule implements Serializable {

    private List<OrderRequest> orders;

    private Map<String, List<String>> filters;

    private static final long serialVersionUID = 1L;
}
