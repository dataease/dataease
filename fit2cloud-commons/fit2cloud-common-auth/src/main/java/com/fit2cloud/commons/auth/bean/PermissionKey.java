package com.fit2cloud.commons.auth.bean;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 作为HashMap key
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionKey {

    private String resourceId;

    private String url;

    /**
     * 重写equals方法必须重写hashCode方法
     * @param anObject
     * @return
     */
    public boolean equals(Object anObject) {
        if (this == anObject)return true;
        if (anObject instanceof PermissionKey){
            PermissionKey current = (PermissionKey) anObject;
            if (current == null || current.resourceId == null)return false;
            return current.resourceId.equals(this.resourceId);
        }
        return false;
    }

    public int hashCode() {
        return resourceId.hashCode();
    }

}
