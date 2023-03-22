package io.dataease.xpack.permissions.login.dao.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@TableName("per_user")
public class LoginUserPO implements Serializable {
    @Serial
    private static final long serialVersionUID = 8203102059840475528L;


    @TableField("id")
    private Long userId;

    private String pwd;
}
