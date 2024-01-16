package io.dataease.api.sync.datasource.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author gin
 * @Date 2021/4/30 10:57 上午
 */
@Getter
@Setter
public class DBTableDTO {
    private String datasourceId;
    private String name;
    private String remark;
    private boolean enableCheck;
    private String datasetPath;
}
