package io.dataease.iam.pojo.entity;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * SysConfig对象 系统配置信息
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("iam_sys_config")
public class SysConfig {
    @TableId(type = IdType.AUTO)
    private Integer id;


    private String title;


    private String loginUrl;


    private String logoutUrl;


    private String dColor;


    private String dLayout;


    private String helpUrl;


    private String manageUrl;


    private String chromeUrl;

    @TableField(exist = false)
    private JSONObject map;

    @JsonIgnore
    private String mapJson;
}
