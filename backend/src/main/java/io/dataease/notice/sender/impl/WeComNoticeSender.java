package io.dataease.notice.sender.impl;

import io.dataease.notice.sender.AbstractNoticeSender;
import io.dataease.commons.utils.LogUtil;
import io.dataease.notice.domain.MessageDetail;
import io.dataease.notice.message.TextMessage;
import io.dataease.notice.sender.NoticeModel;
import io.dataease.notice.util.WxChatbotClient;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class WeComNoticeSender extends AbstractNoticeSender {


    public void sendWechatRobot(MessageDetail messageDetail, String context) {
        List<String> userIds = messageDetail.getUserIds();
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }
        TextMessage message = new TextMessage(context);
        List<String> phoneLists = super.getUserPhones(userIds);
        message.setMentionedMobileList(phoneLists);
        try {
            WxChatbotClient.send(messageDetail.getWebhook(), message);
        } catch (IOException e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    @Override
    public void send(MessageDetail messageDetail, NoticeModel noticeModel) {
        String context = super.getContext(messageDetail, noticeModel);
        sendWechatRobot(messageDetail, context);
    }
}
