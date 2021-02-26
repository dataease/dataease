package io.dataease.notice.sender.impl;


import io.dataease.commons.utils.LogUtil;
import io.dataease.notice.domain.MessageDetail;
import io.dataease.notice.sender.AbstractNoticeSender;
import io.dataease.notice.sender.NoticeModel;
import io.dataease.notice.service.MailService;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;

@Component
public class MailNoticeSender extends AbstractNoticeSender {
    @Resource
    private MailService mailService;

    private void sendMail(MessageDetail messageDetail, String context, NoticeModel noticeModel) throws MessagingException {
        LogUtil.info("发送邮件开始 ");
        JavaMailSenderImpl javaMailSender = mailService.getMailSender();
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(javaMailSender.getUsername());
        LogUtil.info("发件人地址"+javaMailSender.getUsername());
        LogUtil.info("helper"+helper);
        helper.setSubject("MeterSphere " + noticeModel.getSubject());
        List<String> emails = super.getUserEmails(messageDetail.getUserIds());
        String[] users = emails.toArray(new String[0]);
        LogUtil.info("收件人地址: " + emails);
        helper.setText(context, true);
        helper.setTo(users);
        javaMailSender.send(mimeMessage);
    }

    @Override
    public void send(MessageDetail messageDetail, NoticeModel noticeModel) {
        String context = super.getHtmlContext(messageDetail, noticeModel);
        try {
            sendMail(messageDetail, context, noticeModel);
            LogUtil.info("发送邮件结束");
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }
}
