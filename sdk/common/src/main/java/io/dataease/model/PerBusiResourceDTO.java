package io.dataease.model;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@Data
public class PerBusiResourceDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资源ID
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 类型ID
     */
    private Integer rtId;

    /**
     * 所属组织ID
     */
    private Long orgId;

    /**
     * 上级资源ID
     */
    private Long pid;

    /**
     * 寻根路径
     */
    private String rootPath;

    /**
     * 叶子结点
     */
    private Boolean leaf;

    /**
     * 拓展标识
     */
    private Integer extraFlag;

    /**
     * 拓展标识
     */
    private Integer extraFlag1;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 创建者
     */
    private Long creator;

    @Override
    public String toString() {
        return "PerBusiResource{" +
            "id = " + id +
            ", name = " + name +
            ", rtId = " + rtId +
            ", orgId = " + orgId +
            ", pid = " + pid +
            ", rootPath = " + rootPath +
            ", leaf = " + leaf +
            ", extraFlag = " + extraFlag +
            ", createTime = " + createTime +
            ", creator = " + creator +
            "}";
    }
}
