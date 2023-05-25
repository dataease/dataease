package io.dataease.xpack.permissions.auth.dao.ext.entity;


import lombok.Data;

import java.io.Serializable;

@Data
public class BusiResourcePO implements Serializable {

    private Long id;

    private String name;

    private Long pid;

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        BusiResourcePO po = (BusiResourcePO) obj;
        return po.getId().equals(this.id) && po.getPid().equals(this.pid);
    }


}
