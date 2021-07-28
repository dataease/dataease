package io.dataease.controller.message.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class BatchSettingRequest implements Serializable {

    private List<Long> typeIds;

    private Long channelId;

    private Boolean enable;
}
