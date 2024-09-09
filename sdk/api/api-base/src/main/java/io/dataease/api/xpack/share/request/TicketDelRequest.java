package io.dataease.api.xpack.share.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
@Schema(description = "删除Ticket")
@Data
public class TicketDelRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -3978489349675065507L;
    @Schema(description = "Ticket", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ticket;
}
