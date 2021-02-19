package com.fit2cloud.commons.auth.bean;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtPermissionBean  {
    private String path;
    private String permission;
}
