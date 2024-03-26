package io.dataease.dto.panel.link;

import lombok.Data;

import java.io.Serializable;

@Data
public class TicketDto implements Serializable {

    private boolean ticketValid;

    private boolean ticketExp;

    private String args;
}
