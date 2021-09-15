package io.dataease.auth.entity;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class TokenInfo implements Serializable {

    private String username;

    private Long userId;

   /*  private String idToken; */

    public String format(){
        return username + "," +userId;
    }
}
