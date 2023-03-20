package io.dataease.commons.model;

import lombok.Data;
import net.minidev.json.annotate.JsonIgnore;

import java.io.Serializable;

@Data
public class OnlineUserModel implements Serializable {

    private static final long serialVersionUID = 190044376129186283L;

    @JsonIgnore
    private String token;

    private String ip;

    private Long loginTime;
}
