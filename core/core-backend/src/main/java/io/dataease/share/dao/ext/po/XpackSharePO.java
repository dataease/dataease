package io.dataease.share.dao.ext.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class XpackSharePO implements Serializable {
    @Serial
    private static final long serialVersionUID = 7929343371768885789L;

    private Long shareId;

    private Long resourceId;

    private String name;

    private String type;

    private Long creator;

    private Long time;

    private Long exp;

    private Integer extFlag;

}
