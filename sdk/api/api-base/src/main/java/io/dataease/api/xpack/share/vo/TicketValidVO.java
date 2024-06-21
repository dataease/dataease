package io.dataease.api.xpack.share.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class TicketValidVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 2452043685969885580L;

    private boolean ticketValid;

    private boolean ticketExp;

    private String args;
}
