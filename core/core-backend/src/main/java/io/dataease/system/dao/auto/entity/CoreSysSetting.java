package io.dataease.system.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author fit2cloud
 * @since 2023-10-27
 */
@TableName("core_sys_setting")
public class CoreSysSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 键
     */
    private String pkey;

    /**
     * 值
     */
    private String pval;

    /**
     * 类型
     */
    private String type;

    /**
     * 顺序
     */
    private Integer sort;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPkey() {
        return pkey;
    }

    public void setPkey(String pkey) {
        this.pkey = pkey;
    }

    public String getPval() {
        return pval;
    }

    public void setPval(String pval) {
        this.pval = pval;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    @Override
    public String toString() {
        return "CoreSysSetting{" +
        "id = " + id +
        ", pkey = " + pkey +
        ", pval = " + pval +
        ", type = " + type +
        ", sort = " + sort +
        "}";
    }
}
