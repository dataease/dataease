package io.dataease.api.xpack.share.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "分享Ticket")
@Data
public class TicketCreator implements Serializable {
    @Serial
    private static final long serialVersionUID = 8661378104009097296L;

    @Schema(description = "Ticket", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ticket;
    @Schema(description = "有效期", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long exp;
    @Schema(description = "参数")
    private String args;
    @Schema(description = "分享链接UUID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String uuid;
    @Schema(description = "刷新Ticket")
    private boolean generateNew;
}
