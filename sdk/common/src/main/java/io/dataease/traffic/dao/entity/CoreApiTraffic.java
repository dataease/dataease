package io.dataease.traffic.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@TableName("core_api_traffic")
@Data
public class CoreApiTraffic implements Serializable {
    @Serial
    private static final long serialVersionUID = -9130425144350145905L;

    private Long id;

    private String api;

    private Integer threshold;

    private Integer alive;
}
