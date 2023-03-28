package io.dataease.datasource.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 驱动
 * </p>
 *
 * @author fit2cloud
 * @since 2023-03-16 18:11:28
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("core_driver")
@ApiModel(value = "CoreDriver对象", description = "驱动")
public class CoreDriver implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @ApiModelProperty("名称")
    @TableField("`name`")
    private String name;

    @ApiModelProperty("创健时间")
    @TableField("create_time")
    private Long createTime;

    @ApiModelProperty("数据源类型")
    @TableField("`type`")
    private String type;

    @ApiModelProperty("驱动类")
    @TableField("driver_class")
    private String driverClass;

    @ApiModelProperty("描述")
    @TableField("`desc`")
    private String desc;
}
