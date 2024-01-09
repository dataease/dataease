package io.dataease.api.permissions.org.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrgDetailVO {

    private Long id;

    private String name;

    private Long pid;

    private String rootPath;

}
