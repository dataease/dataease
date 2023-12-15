package io.dataease.api.sync.datasource.vo.model;

import lombok.Data;

/**
 * @author fit2cloud
 * @date 2023/8/10 16:38
 **/
@Data
public class SeaTunnelJobParam {

    private String jobId;
    private Source source;
    private Target target;
    private String seaTunnelConfig;
}
