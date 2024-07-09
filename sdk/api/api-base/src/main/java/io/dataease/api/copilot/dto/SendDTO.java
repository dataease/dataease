package io.dataease.api.copilot.dto;

import lombok.Data;

import java.util.List;

/**
 * @Author Junjun
 */
@Data
public class SendDTO {
    private String engine;
    private String schema;
    private String question;
    private List<HistoryDTO> history;
}
