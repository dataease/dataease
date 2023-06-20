package io.dataease.datasource.dto;

import io.dataease.datasource.dao.auto.entity.CoreDatasourceTask;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author gin
 * @Date 2021/3/9 3:19 下午
 */
@Getter
@Setter
public class CoreDatasourceTaskDTO extends CoreDatasourceTask {
    private String datasourceName;
    private Long nextExecTime;
    private String taskStatus;
    private String msg;
    private String privileges;
}
