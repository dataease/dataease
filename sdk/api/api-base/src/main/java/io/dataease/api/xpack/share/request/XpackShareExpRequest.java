package io.dataease.api.xpack.share.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class XpackShareExpRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 5519219260721146347L;

    private Long resourceId;

    private Long exp;
}
