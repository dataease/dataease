package io.dataease.xpack.permissions.auth.dao.ext.entity;


import io.dataease.model.TreeBaseModel;
import lombok.Data;

import java.io.Serializable;

@Data
public class BusiResourcePO implements TreeBaseModel<BusiResourcePO>, Serializable {

    private Long id;

    private String name;

    private Boolean leaf;
    private Long pid;

    private Integer extraFlag;

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        BusiResourcePO po = (BusiResourcePO) obj;
        return po.getId().equals(this.id) && po.getPid().equals(this.pid);
    }


}
