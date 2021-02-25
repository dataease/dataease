package io.dataease.notice.sender;

import io.dataease.notice.domain.MessageDetail;
import org.springframework.scheduling.annotation.Async;

public interface NoticeSender {
    @Async
    void send(MessageDetail messageDetail, NoticeModel noticeModel);
}
