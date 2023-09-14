package io.dataease.plugins.xpack.email.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class XpackEmailTaskGridRequest extends XpackEmailInstanceGridRequest {


    private List<Long> userIdList;

}
