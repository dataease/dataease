package io.dataease.datasource.dao.ext.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("core_datasource")
public class DataSourceNodePO implements Serializable {

    @TableId
    private Long id;

    private String name;

    private String type;

    private String status;

    private Long createTime;
}
