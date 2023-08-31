package io.dataease.api.xpack.share.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class XpackShareProxyRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 7758730984988104057L;

    private String uuid;

    private String ciphertext;
}
