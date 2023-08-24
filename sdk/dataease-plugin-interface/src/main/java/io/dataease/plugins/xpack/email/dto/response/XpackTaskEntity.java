package io.dataease.plugins.xpack.email.dto.response;

import io.dataease.plugins.xpack.email.dto.request.XpackTaskCreateRequest;
import lombok.Data;

@Data
public class XpackTaskEntity extends XpackTaskCreateRequest {

    private Boolean status;
}
