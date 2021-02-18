package com.fit2cloud.commons.auth.bean;

import com.fit2cloud.commons.auth.entity.Permission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtPermissionBean extends Permission {
    private String url;

    public PermissionKey getKey(){
        return PermissionKey.builder().resourceId(getResourceId()).url(url).build();
    }
}
