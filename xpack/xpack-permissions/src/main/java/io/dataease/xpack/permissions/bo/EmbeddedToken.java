package io.dataease.xpack.permissions.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddedToken implements Serializable {

    private String account;

    private String appId;

}
