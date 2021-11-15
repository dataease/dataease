package io.dataease.service.message.service.strategy;

import io.dataease.auth.service.AuthUserService;
import io.dataease.service.message.service.SendService;
import io.dataease.service.system.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service("sendEmail")
public class SendEmail implements SendService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuthUserService authUserService;


    @Async
    @Override
    public void sendMsg(Long userId, Long typeId, String content, String param) {
        String email = authUserService.getUserById(userId).getEmail();

        emailService.send(email, content, content);
    }
}
