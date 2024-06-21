package io.dataease.api.xpack.share.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class TicketVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -599110079356725271L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String uuid;

    private String ticket;

    private Long exp;

    private String args;

    private Long accessTime;
}
