package io.dataease.api.xpack.share.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class TicketDelRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -3978489349675065507L;

    private String ticket;
}
