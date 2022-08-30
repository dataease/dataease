package io.dataease.service.sys.log;

import io.dataease.ext.query.GridExample;
import lombok.Data;

import java.util.List;

@Data
public class LogQueryParam extends GridExample {

    private List<String> unionIds;
}
