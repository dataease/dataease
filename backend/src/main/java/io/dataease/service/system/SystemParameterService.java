package io.dataease.service.system;

import com.alibaba.fastjson.JSON;
import io.dataease.base.domain.FileMetadata;
import io.dataease.base.domain.SystemParameter;
import io.dataease.base.domain.SystemParameterExample;
import io.dataease.base.mapper.SystemParameterMapper;
import io.dataease.base.mapper.ext.ExtSystemParameterMapper;
import io.dataease.commons.constants.ParamConstants;
import io.dataease.commons.exception.DEException;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.EncryptUtils;
import io.dataease.commons.utils.LogUtil;
import io.dataease.dto.SystemParameterDTO;
import io.dataease.i18n.Translator;
import io.dataease.service.FileService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


@Service
@Transactional(rollbackFor = Exception.class)
public class SystemParameterService {

    @Resource
    private SystemParameterMapper systemParameterMapper;
    @Resource
    private ExtSystemParameterMapper extSystemParameterMapper;
    @Resource
    private FileService fileService;


    public String searchEmail() {
        return extSystemParameterMapper.email();
    }

    public String getSystemLanguage() {
        String result = StringUtils.EMPTY;
        SystemParameterExample example = new SystemParameterExample();
        example.createCriteria().andParamKeyEqualTo(ParamConstants.I18n.LANGUAGE.getValue());
        List<SystemParameter> list = systemParameterMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(list)) {
            String value = list.get(0).getParamValue();
            if (StringUtils.isNotBlank(value)) {
                result = value;
            }
        }
        return result;
    }

    public void editMail(List<SystemParameter> parameters) {
        List<SystemParameter> paramList = this.getParamList(ParamConstants.Classify.MAIL.getValue());
        boolean empty = paramList.size() <= 0;

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

    public List<SystemParameter> getParamList(String type) {
        SystemParameterExample example = new SystemParameterExample();
        example.createCriteria().andParamKeyLike(type + "%");
        return systemParameterMapper.selectByExample(example);
    }

    public void testConnection(HashMap<String, String> hashMap) {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setDefaultEncoding("UTF-8");
        javaMailSender.setHost(hashMap.get(ParamConstants.MAIL.SERVER.getValue()));
        javaMailSender.setPort(Integer.valueOf(hashMap.get(ParamConstants.MAIL.PORT.getValue())));
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
        props.put("mail.smtp.connectiontimeout", "5000");
        javaMailSender.setJavaMailProperties(props);
        try {
            javaMailSender.testConnection();
        } catch (MessagingException e) {
            LogUtil.error(e.getMessage(), e);
            DEException.throwException(Translator.get("connection_failed"));
        }
        if(!StringUtils.isBlank(recipients)){
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = null;
            try {
                helper = new MimeMessageHelper(mimeMessage, true);
                helper.setFrom(javaMailSender.getUsername());
                helper.setSubject("MeterSphere测试邮件 " );
                helper.setText("这是一封测试邮件，邮件发送成功", true);
                helper.setTo(recipients);
                javaMailSender.send(mimeMessage);
            } catch (MessagingException e) {
                LogUtil.error(e.getMessage(), e);
                DEException.throwException(Translator.get("connection_failed"));
            }
        }


    }

    public String getVersion() {
        return System.getenv("MS_VERSION");
    }



    public void saveLdap(List<SystemParameter> parameters) {
        SystemParameterExample example = new SystemParameterExample();
        parameters.forEach(param -> {
            if (param.getParamKey().equals(ParamConstants.LDAP.PASSWORD.getValue())) {
                String string = EncryptUtils.aesEncrypt(param.getParamValue()).toString();
                param.setParamValue(string);
            }
            example.createCriteria().andParamKeyEqualTo(param.getParamKey());
            if (systemParameterMapper.countByExample(example) > 0) {
                systemParameterMapper.updateByPrimaryKey(param);
            } else {
                systemParameterMapper.insert(param);
            }
            example.clear();
        });
    }

    public String getValue(String key) {
        SystemParameter param = systemParameterMapper.selectByPrimaryKey(key);
        if (param == null) {
            return null;
        }
        return param.getParamValue();
    }


    public List<SystemParameterDTO> getSystemParameterInfo(String paramConstantsType) {
        List<SystemParameter> paramList = this.getParamList(paramConstantsType);
        List<SystemParameterDTO> dtoList = new ArrayList<>();
        for (SystemParameter systemParameter : paramList) {
            SystemParameterDTO systemParameterDTO = new SystemParameterDTO();
            BeanUtils.copyBean(systemParameterDTO, systemParameter);
            if (systemParameter.getType().equalsIgnoreCase("file")) {
                FileMetadata fileMetadata = fileService.getFileMetadataById(systemParameter.getParamValue());
                if (fileMetadata != null) {
                    systemParameterDTO.setFileName(fileMetadata.getName());
                }
            }
            dtoList.add(systemParameterDTO);
        }
        dtoList.sort(Comparator.comparingInt(SystemParameter::getSort));
        return dtoList;
    }



    public void saveUIInfo(Map<String,List<SystemParameterDTO>> request, List<MultipartFile> bodyFiles) throws IOException {
        List<SystemParameterDTO> parameters = request.get("systemParams");
        if (null != bodyFiles)
        for (MultipartFile multipartFile : bodyFiles) {
            if (!multipartFile.isEmpty()) {
                //防止添加非图片文件
                try (InputStream input = multipartFile.getInputStream()) {
                    try {
                        // It's an image (only BMP, GIF, JPG and PNG are recognized).
                        ImageIO.read(input).toString();
                    } catch (Exception e) {
                        DEException.throwException("Uploaded images do not meet the image format requirements");
                        return;
                    }
                }
                String multipartFileName = multipartFile.getOriginalFilename();
                String[] split = Objects.requireNonNull(multipartFileName).split(",");
                parameters.stream().filter(systemParameterDTO -> systemParameterDTO.getParamKey().equalsIgnoreCase(split[1])).forEach(systemParameterDTO -> {
                    systemParameterDTO.setFileName(split[0]);
                    systemParameterDTO.setFile(multipartFile);
                });
            }
        }
        for (SystemParameterDTO systemParameter : parameters) {
            MultipartFile file = systemParameter.getFile();
            if (systemParameter.getType().equalsIgnoreCase("file")) {
                if (StringUtils.isBlank(systemParameter.getFileName())) {
                    fileService.deleteFileById(systemParameter.getParamValue());
                }
                if (file != null) {
                    fileService.deleteFileById(systemParameter.getParamValue());
                    FileMetadata fileMetadata = fileService.saveFile(systemParameter.getFile(),systemParameter.getFileName());
                    systemParameter.setParamValue(fileMetadata.getId());
                }
                if (file == null && systemParameter.getFileName() == null) {
                    systemParameter.setParamValue(null);
                }
            }
            systemParameterMapper.deleteByPrimaryKey(systemParameter.getParamKey());
            systemParameterMapper.insert(systemParameter);
        }

    }

    public static void main(String[] args) {
        String info="[{\"paramKey\":\"base.url\",\"paramValue\":null,\"type\":\"text\",\"sort\":1,\"file\":null,\"fileName\":null},{\"paramKey\":\"base.title\",\"paramValue\":\"DataEase Title\",\"type\":\"text\",\"sort\":3,\"file\":null,\"fileName\":null},{\"paramKey\":\"base.logo\",\"paramValue\":\"DataEase\",\"type\":\"text\",\"sort\":4,\"file\":null,\"fileName\":\"favicon.icon.png\"}]";
        List<SystemParameterDTO> temp = JSON.parseArray(info,SystemParameterDTO.class);
//        System.out.println("===>");
    }
}
