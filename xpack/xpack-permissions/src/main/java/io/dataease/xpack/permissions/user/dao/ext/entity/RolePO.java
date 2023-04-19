package io.dataease.xpack.permissions.user.dao.ext.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;


@TableName("per_role")
@Data
public class RolePO implements Serializable {

    private Long id;
    private String name;
    private boolean readonly;
    private Long pid;
}
