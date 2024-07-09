package io.dataease.api.copilot.dto;

import lombok.Data;

import java.util.Map;

/**
 * @Author Junjun
 */
@Data
public class DEReceiveDTO extends ReceiveDTO {
    private Map<String, Object> chartData;
}
