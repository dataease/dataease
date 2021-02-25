package io.dataease.notice.controller;

import io.dataease.notice.domain.MessageDetail;
import io.dataease.notice.service.NoticeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("notice")
public class NoticeController {
    @Resource
    private NoticeService noticeService;

    @PostMapping("save/message/task")
    public void saveMessage(@RequestBody MessageDetail messageDetail) {
        noticeService.saveMessageTask(messageDetail);
    }

    @GetMapping("/search/message/type/{type}")
    public List<MessageDetail> searchMessage(@PathVariable String type) {
        return noticeService.searchMessageByType(type);
    }

    @GetMapping("/search/message/{testId}")
    public List<MessageDetail> searchMessageSchedule(@PathVariable String testId) {
        return noticeService.searchMessageByTestId(testId);
    }

    @GetMapping("/delete/message/{identification}")
    public int deleteMessage(@PathVariable String identification) {
        return noticeService.delMessage(identification);
    }
}

