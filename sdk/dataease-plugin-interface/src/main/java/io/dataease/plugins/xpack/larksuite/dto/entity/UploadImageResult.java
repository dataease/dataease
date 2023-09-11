package io.dataease.plugins.xpack.larksuite.dto.entity;

import io.dataease.plugins.xpack.lark.dto.entity.LarkBaseResult;
import lombok.Data;

import java.io.Serializable;

@Data
public class UploadImageResult extends LarkBaseResult implements Serializable {

    private UploadImageData data;
}
