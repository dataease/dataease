package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class PanelLinkTicket implements Serializable {
    private Long id;

    private String uuid;

    private String ticket;

    private Long exp;

    private String args;

    private Long accessTime;

    private static final long serialVersionUID = 1L;
}