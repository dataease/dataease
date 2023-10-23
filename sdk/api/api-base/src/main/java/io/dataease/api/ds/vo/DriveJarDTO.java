package io.dataease.api.ds.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class DriveJarDTO implements Serializable {


    @Serial
    private static final long serialVersionUID = 1175287571828910222L;

    private Long id;

    private String name;

    private String type;

    private String typeDesc;

    private String desc;
}
