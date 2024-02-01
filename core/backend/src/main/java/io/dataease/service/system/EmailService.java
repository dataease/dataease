package io.dataease.service.system;

import io.dataease.commons.constants.ParamConstants;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.commons.utils.EncryptUtils;
import io.dataease.commons.utils.LogUtil;
import io.dataease.controller.sys.response.MailInfo;
import io.dataease.i18n.Translator;
import io.dataease.plugins.common.base.domain.SystemParameter;
import io.dataease.plugins.common.base.domain.SystemParameterExample;
import io.dataease.plugins.common.base.mapper.SystemParameterMapper;
import io.dataease.plugins.common.exception.DataEaseException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.annotation.Resource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Service
public class EmailService {

    private static final String SSL_CLASS_KEY = "mail.smtp.socketFactory.class";

    private static final String SSL_CLASS_VAL = "javax.net.ssl.SSLSocketFactory";

    private static final String TLS_PROP_KEY = "mail.smtp.starttls.enable";

    private static final String SMTP_TIMEOUT_KEY = "mail.smtp.timeout";

    private static final String SMTP_TIMEOUT_VAL = "30000";

    private static final String SMTP_CONNECTIONTIMEOUT_KEY = "mail.smtp.connectiontimeout";

    private static final String SMTP_CONNECTIONTIMEOUT_VAL = "5000";

    @Resource
    private SystemParameterMapper systemParameterMapper;

    /**
     * @param to      收件人
     * @param title   标题
     * @param content 内容
     */
    public void send(String to, String title, String content) {
        if (StringUtils.isBlank(to))
            return;
        MailInfo mailInfo = proxy().mailInfo();
        checkMailInfo(mailInfo);
        JavaMailSenderImpl driver = driver(mailInfo);

        MimeMessage mimeMessage = driver.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(driver.getUsername());
            helper.setSubject(title);
            helper.setText(content, true);
            helper.setTo(to);
            driver.send(mimeMessage);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            DataEaseException.throwException(Translator.get("connection_failed"));
        }
    }


    public void sendPdfWithFiles(String to, String title, String content, byte[] bytes, List<File> files) {
        if (ArrayUtils.isEmpty(bytes)) {
            send(to, title, content);
            return;
        }
        if (StringUtils.isBlank(to))
            return;
        MailInfo mailInfo = proxy().mailInfo();
        checkMailInfo(mailInfo);
        JavaMailSenderImpl driver = driver(mailInfo);

        MimeMultipart multipart = new MimeMultipart();
        MimeMessage mimeMessage = driver.createMimeMessage();
        try {
            multipart = addContent(multipart, content);
            multipart = addPdf(multipart, bytes);
            if (CollectionUtils.isNotEmpty(files)) {
                multipart = addFiles(multipart, files);
            }
            mimeMessage.setSubject(title);
            mimeMessage.setFrom(driver.getUsername());
            mimeMessage.setRecipients(Message.RecipientType.TO, to);
            mimeMessage.setContent(multipart);
            driver.send(mimeMessage);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            DataEaseException.throwException(e);
        }
    }

    public void sendWithImageAndFiles(String to, String title, String content, byte[] bytes, List<File> files) {
        if (StringUtils.isBlank(to))
            return;

        if (ArrayUtils.isEmpty(bytes)) {
            send(to, title, content);
            return;
        }
        MailInfo mailInfo = proxy().mailInfo();
        checkMailInfo(mailInfo);
        JavaMailSenderImpl driver = driver(mailInfo);
        MimeMessage mimeMessage = driver.createMimeMessage();
        MimeMultipart multipart = new MimeMultipart();

        try {
            multipart = addImage(multipart, bytes, content);
            if (CollectionUtils.isNotEmpty(files)) {
                multipart = addFiles(multipart, files);
            }
            mimeMessage.setFrom(driver.getUsername());
            mimeMessage.setSubject(title);
            mimeMessage.setRecipients(Message.RecipientType.TO, to);
            mimeMessage.setContent(multipart);
            driver.send(mimeMessage);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            DataEaseException.throwException(e);
        }
    }

    private MimeMultipart addContent(MimeMultipart multipart, String content) throws Exception {
        MimeBodyPart text = new MimeBodyPart();
        text.setContent(content, "text/html; charset=gb2312");
        multipart.addBodyPart(text);
        return multipart;
    }

    private MimeMultipart addImage(MimeMultipart multipart, byte[] bytes, String content) throws Exception {
        MimeBodyPart image = new MimeBodyPart();
        DataHandler png = new DataHandler(new ByteArrayDataSource(bytes, "image/png"));
        String uuid = UUID.randomUUID().toString();
        MimeBodyPart text = new MimeBodyPart();
        text.setContent(content + "<br/><img style='width: 60%;' src='cid:" + uuid + "' />", "text/html; charset=gb2312");
        image.setDataHandler(png);
        image.setContentID(uuid);

        multipart.addBodyPart(text);
        multipart.addBodyPart(image);
        multipart.setSubType("related");
        return multipart;
    }

    private MimeMultipart addPdf(MimeMultipart multipart, byte[] bytes) throws Exception {
        MimeBodyPart attach = new MimeBodyPart();
        ByteArrayDataSource fileDataSource = new ByteArrayDataSource(bytes, "application/pdf");
        attach.setDataHandler(new DataHandler(fileDataSource));
        attach.setFileName(MimeUtility.encodeText("panel.pdf", "gb2312", null));
        multipart.addBodyPart(attach);
        multipart.setSubType("related");
        return multipart;
    }

    private MimeMultipart addFiles(MimeMultipart multipart, List<File> files) throws Exception {

        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            MimeBodyPart attach = new MimeBodyPart();
            FileDataSource fileDataSource = new FileDataSource(file);
            attach.setDataHandler(new DataHandler(fileDataSource));
            attach.setFileName(MimeUtility.encodeText(file.getName(), "gb2312", null));
            multipart.addBodyPart(attach);
        }

        multipart.setSubType("related");
        return multipart;
    }

    public JavaMailSenderImpl driver(MailInfo mailInfo) {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setDefaultEncoding("UTF-8");
        javaMailSender.setHost(mailInfo.getHost());
        javaMailSender.setPort(Integer.parseInt(mailInfo.getPort()));
        javaMailSender.setUsername(mailInfo.getAccount());
        javaMailSender.setPassword(mailInfo.getPassword());
        Properties props = new Properties();
        if (BooleanUtils.toBoolean(mailInfo.getSsl())) {
            props.put(SSL_CLASS_KEY, SSL_CLASS_VAL);
        }
        if (BooleanUtils.toBoolean(mailInfo.getTls())) {
            props.put(TLS_PROP_KEY, "true");
        }
        props.put(SMTP_TIMEOUT_KEY, SMTP_TIMEOUT_VAL);
        props.put(SMTP_CONNECTIONTIMEOUT_KEY, SMTP_CONNECTIONTIMEOUT_VAL);
        javaMailSender.setJavaMailProperties(props);
        return javaMailSender;
    }

    private EmailService proxy() {
        return CommonBeanFactory.getBean(EmailService.class);
    }

    public MailInfo mailInfo() {
        System.getProperties().setProperty("mail.mime.splitlongparameters", "false");
        String type = ParamConstants.Classify.MAIL.getValue();
        List<SystemParameter> paramList = getParamList(type);
        MailInfo mailInfo = new MailInfo();
        if (!CollectionUtils.isEmpty(paramList)) {
            for (SystemParameter param : paramList) {
                if (StringUtils.equals(param.getParamKey(), ParamConstants.MAIL.SERVER.getValue())) {
                    mailInfo.setHost(param.getParamValue());
                } else if (StringUtils.equals(param.getParamKey(), ParamConstants.MAIL.PORT.getValue())) {
                    mailInfo.setPort(param.getParamValue());
                } else if (StringUtils.equals(param.getParamKey(), ParamConstants.MAIL.ACCOUNT.getValue())) {
                    mailInfo.setAccount(param.getParamValue());
                } else if (StringUtils.equals(param.getParamKey(), ParamConstants.MAIL.PASSWORD.getValue())) {
                    String password = EncryptUtils.aesDecrypt(param.getParamValue()).toString();
                    mailInfo.setPassword(password);
                } else if (StringUtils.equals(param.getParamKey(), ParamConstants.MAIL.SSL.getValue())) {
                    mailInfo.setSsl(param.getParamValue());
                } else if (StringUtils.equals(param.getParamKey(), ParamConstants.MAIL.TLS.getValue())) {
                    mailInfo.setTls(param.getParamValue());
                } else if (StringUtils.equals(param.getParamKey(), ParamConstants.MAIL.RECIPIENTS.getValue())) {
                    mailInfo.setRecipient(param.getParamValue());
                }
            }
        }

        return mailInfo;
    }

    public void checkMailInfo(MailInfo info) {

        Assert.notNull(info, Translator.get("I18N_EMAIL_CONFIG_ERROR"));
        Assert.notNull(info.getHost(), Translator.get("I18N_EMAIL_HOST_ERROR"));
        Assert.notNull(info.getPort(), Translator.get("I18N_EMAIL_PORT_ERROR"));
        Assert.notNull(info.getAccount(), Translator.get("I18N_EMAIL_ACCOUNT_ERROR"));
    }

    public List<SystemParameter> getParamList(String type) {
        SystemParameterExample example = new SystemParameterExample();
        example.createCriteria().andParamKeyLike(type + "%");
        return systemParameterMapper.selectByExample(example);
    }

    public void editMail(List<SystemParameter> parameters) {
        parameters.forEach(parameter -> {
            SystemParameterExample example = new SystemParameterExample();
            if (parameter.getParamKey().equals(ParamConstants.MAIL.PASSWORD.getValue())) {
                if (!StringUtils.isBlank(parameter.getParamValue())) {
                    String string = EncryptUtils.aesEncrypt(parameter.getParamValue()).toString();
                    parameter.setParamValue(string);
                }
            }
            example.createCriteria().andParamKeyEqualTo(parameter.getParamKey());
            if (systemParameterMapper.countByExample(example) > 0) {
                systemParameterMapper.updateByPrimaryKey(parameter);
            } else {
                systemParameterMapper.insert(parameter);
            }
            example.clear();

        });
    }

    private JavaMailSenderImpl buildSender() {
        MailInfo mailInfo = proxy().mailInfo();
        checkMailInfo(mailInfo);
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setDefaultEncoding("UTF-8");
        javaMailSender.setHost(mailInfo.getHost());
        javaMailSender.setPort(Integer.parseInt(mailInfo.getPort()));
        javaMailSender.setUsername(mailInfo.getAccount());
        javaMailSender.setPassword(mailInfo.getPassword());
        Properties props = new Properties();
        if (BooleanUtils.toBoolean(mailInfo.getSsl())) {
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }
        if (BooleanUtils.toBoolean(mailInfo.getTls())) {
            props.put("mail.smtp.starttls.enable", "true");
        }
        props.put("mail.smtp.timeout", "30000");
        props.put("mail.smtp.connectiontimeout", "10000");
        javaMailSender.setJavaMailProperties(props);
        return javaMailSender;
    }

    private void testSendEmail(String recipients, JavaMailSenderImpl javaMailSender, boolean isAdmin) {
        if (!StringUtils.isBlank(recipients)) {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper;
            try {
                helper = new MimeMessageHelper(mimeMessage, true);
                helper.setFrom(javaMailSender.getUsername());
                helper.setSubject("DataEase测试邮件 ");
                helper.setText("这是一封测试邮件，邮件发送成功", true);
                helper.setTo(recipients);
                javaMailSender.send(mimeMessage);
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
                String key = "connection_failed";
                if (isAdmin) key = "connection_failed_admin";
                DataEaseException.throwException(Translator.get(key));
            }
        }
    }

    public void testConnection(String email) {
        JavaMailSenderImpl javaMailSender = null;
        try {
            javaMailSender = buildSender();
            javaMailSender.testConnection();
        } catch (MessagingException e) {
            LogUtil.error(e.getMessage(), e);
            DataEaseException.throwException(Translator.get("connection_failed"));
        }
        testSendEmail(email, javaMailSender, true);
    }

    public void testConnection(HashMap<String, String> hashMap) {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setDefaultEncoding("UTF-8");
        javaMailSender.setHost(hashMap.get(ParamConstants.MAIL.SERVER.getValue()));
        javaMailSender.setPort(Integer.parseInt(hashMap.get(ParamConstants.MAIL.PORT.getValue())));
        javaMailSender.setUsername(hashMap.get(ParamConstants.MAIL.ACCOUNT.getValue()));
        javaMailSender.setPassword(hashMap.get(ParamConstants.MAIL.PASSWORD.getValue()));
        Properties props = new Properties();
        String recipients = hashMap.get(ParamConstants.MAIL.RECIPIENTS.getValue());
        if (BooleanUtils.toBoolean(hashMap.get(ParamConstants.MAIL.SSL.getValue()))) {
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }
        if (BooleanUtils.toBoolean(hashMap.get(ParamConstants.MAIL.TLS.getValue()))) {
            props.put("mail.smtp.starttls.enable", "true");
        }
        props.put("mail.smtp.timeout", "30000");
        props.put("mail.smtp.connectiontimeout", "10000");
        javaMailSender.setJavaMailProperties(props);
        try {
            javaMailSender.testConnection();
        } catch (MessagingException e) {
            LogUtil.error(e.getMessage(), e);
            DataEaseException.throwException(Translator.get("connection_failed"));
        }
        testSendEmail(recipients, javaMailSender, false);
    }
}
