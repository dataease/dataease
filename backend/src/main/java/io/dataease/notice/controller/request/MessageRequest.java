package io.dataease.notice.controller.request;

import io.dataease.notice.domain.MessageDetail;
import lombok.Data;

import java.util.List;

@Data
public class MessageRequest {
    private List<MessageDetail> messageDetail;
}
