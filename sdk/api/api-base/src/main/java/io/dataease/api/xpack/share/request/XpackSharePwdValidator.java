package io.dataease.api.xpack.share.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class XpackSharePwdValidator implements Serializable {
    @Serial
    private static final long serialVersionUID = 5723073697210793005L;


    private String ciphertext;
}
