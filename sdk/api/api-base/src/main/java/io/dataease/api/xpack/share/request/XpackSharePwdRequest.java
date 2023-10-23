package io.dataease.api.xpack.share.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class XpackSharePwdRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -4399320897911936623L;

    private Long resourceId;

    private String pwd;
}
