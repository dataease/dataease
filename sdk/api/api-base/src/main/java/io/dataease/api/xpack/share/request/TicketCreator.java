package io.dataease.api.xpack.share.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class TicketCreator implements Serializable {
    @Serial
    private static final long serialVersionUID = 8661378104009097296L;

    private String ticket;

    private Long exp;

    private String args;

    private String uuid;

    private boolean generateNew;
}
