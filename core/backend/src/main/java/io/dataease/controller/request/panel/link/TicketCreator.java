package io.dataease.controller.request.panel.link;

import lombok.Data;

import java.io.Serializable;

@Data
public class TicketCreator implements Serializable {

    private String ticket;

    private Long exp;

    private String args;

    private String uuid;

    private boolean generateNew;
}
