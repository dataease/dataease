package io.dataease.notice.service;

import io.dataease.base.domain.SystemParameter;
import io.dataease.commons.constants.ParamConstants;
import io.dataease.commons.utils.EncryptUtils;
import io.dataease.service.system.SystemParameterService;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Properties;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class MailService {
    @Resource
    private SystemParameterService systemParameterService;

    public JavaMailSenderImpl getMailSender() {
        Properties props = new Properties();
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        List<SystemParameter> paramList = systemParameterService.getParamList(ParamConstants.Classify.MAIL.getValue());
        javaMailSender.setDefaultEncoding("UTF-8");
        javaMailSender.setProtocol("smtp");
        props.put("mail.smtp.auth", "true");

        for (SystemParameter p : paramList) {
            switch (p.getParamKey()) {
                case "smtp.host":
                    javaMailSender.setHost(p.getParamValue());
                    break;
                case "smtp.port":
                    javaMailSender.setPort(Integer.parseInt(p.getParamValue()));
                    break;
                case "smtp.account":
                    javaMailSender.setUsername(p.getParamValue());
                    break;
                case "smtp.password":
                    javaMailSender.setPassword(EncryptUtils.aesDecrypt(p.getParamValue()).toString());
                    break;
                case "smtp.ssl":
                    if (BooleanUtils.toBoolean(p.getParamValue())) {
                        javaMailSender.setProtocol("smtps");
                        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                    }
                    break;
                case "smtp.tls":
                    String result = BooleanUtils.toString(BooleanUtils.toBoolean(p.getParamValue()), "true", "false");
                    props.put("mail.smtp.starttls.enable", result);
                    props.put("mail.smtp.starttls.required", result);
                    break;
             /*   case "smtp.anon":
                    boolean isAnon = BooleanUtils.toBoolean(p.getParamValue());
                    if (isAnon) {
                        props.put("mail.smtp.auth", "false");
                        javaMailSender.setUsername(null);
                        javaMailSender.setPassword(null);
                    }
                    break;*/
                default:
                    break;
            }
        }

        props.put("mail.smtp.timeout", "30000");
        props.put("mail.smtp.connectiontimeout", "5000");
        javaMailSender.setJavaMailProperties(props);
        return javaMailSender;
    }
}





