package io.dataease.service.system;

import io.dataease.commons.constants.ParamConstants;
import io.dataease.commons.exception.DEException;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.EncryptUtils;
import io.dataease.controller.sys.response.BasicInfo;
import io.dataease.dto.SystemParameterDTO;
import io.dataease.exception.DataEaseException;
import io.dataease.plugins.common.base.domain.FileMetadata;
import io.dataease.plugins.common.base.domain.SystemParameter;
import io.dataease.plugins.common.base.domain.SystemParameterExample;
import io.dataease.plugins.common.base.mapper.SystemParameterMapper;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.cas.dto.CasSaveResult;
import io.dataease.plugins.xpack.cas.service.CasXpackService;
import io.dataease.plugins.xpack.display.service.DisplayXpackService;
import io.dataease.plugins.xpack.loginlimit.service.LoginLimitXpackService;
import io.dataease.service.FileService;
import io.dataease.service.datasource.DatasourceService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import io.dataease.ext.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class SystemParameterService {

    private final static String LOGIN_TYPE_KEY = "basic.loginType";
    private final static String CAS_LOGIN_TYPE = "3";
    @Resource
    private SystemParameterMapper systemParameterMapper;
    @Resource
    private ExtSystemParameterMapper extSystemParameterMapper;
    @Resource
    private FileService fileService;
    @Resource
    @Lazy
    private DatasourceService datasourceService;

    public String searchEmail() {
        return extSystemParameterMapper.email();
    }

    public BasicInfo basicInfo() {
        List<SystemParameter> paramList = this.getParamList("basic");
        List<SystemParameter> homePageList = this.getParamList("ui.openHomePage");
        List<SystemParameter> marketPageList = this.getParamList("ui.openMarketPage");
        List<SystemParameter> loginLimitList = this.getParamList("loginlimit");
        paramList.addAll(homePageList);
        paramList.addAll(marketPageList);
        paramList.addAll(loginLimitList);
        BasicInfo result = new BasicInfo();
        result.setOpenHomePage("true");
        Map<String, LoginLimitXpackService> beansOfType = SpringContextUtil.getApplicationContext().getBeansOfType((LoginLimitXpackService.class));
        Boolean loginLimitPluginLoaded = beansOfType.keySet().size() > 0;
        if (!CollectionUtils.isEmpty(paramList)) {
            for (SystemParameter param : paramList) {
                if (StringUtils.equals(param.getParamKey(), ParamConstants.BASIC.FRONT_TIME_OUT.getValue())) {
                    result.setFrontTimeOut(param.getParamValue());
                }
                if (StringUtils.equals(param.getParamKey(), ParamConstants.BASIC.MSG_TIME_OUT.getValue())) {
                    result.setMsgTimeOut(param.getParamValue());
                }
                if (StringUtils.equals(param.getParamKey(), ParamConstants.BASIC.DEFAULT_LOGIN_TYPE.getValue())) {
                    String paramValue = param.getParamValue();
                    result.setLoginType(StringUtils.isNotBlank(paramValue) ? Integer.parseInt(paramValue) : 0);
                }
                if (StringUtils.equals(param.getParamKey(), ParamConstants.BASIC.OPEN_HOME_PAGE.getValue())) {
                    boolean open = StringUtils.equals("true", param.getParamValue());
                    result.setOpenHomePage(open ? "true" : "false");
                }
                if (StringUtils.equals(param.getParamKey(), ParamConstants.BASIC.OPEN_MARKET_PAGE.getValue())) {
                    boolean open = StringUtils.equals("true", param.getParamValue());
                    result.setOpenMarketPage(open ? "true" : "false");
                }
                if (StringUtils.equals(param.getParamKey(), ParamConstants.BASIC.TEMPLATE_MARKET_ULR.getValue())) {
                    result.setTemplateMarketUlr(param.getParamValue());
                }
                if (StringUtils.equals(param.getParamKey(), ParamConstants.BASIC.TEMPLATE_ACCESS_KEY.getValue())) {
                    result.setTemplateAccessKey(param.getParamValue());
                }
                if (StringUtils.equals(param.getParamKey(), ParamConstants.BASIC.DS_CHECK_INTERVAL.getValue())) {
                    result.setDsCheckInterval(param.getParamValue());
                }
                if (StringUtils.equals(param.getParamKey(), ParamConstants.BASIC.DS_CHECK_INTERVAL_TYPE.getValue())) {
                    result.setDsCheckIntervalType(param.getParamValue());
                }


                if (loginLimitPluginLoaded) {
                    if (StringUtils.equals(param.getParamKey(), ParamConstants.BASIC.LOGIN_LIMIT_LIMITTIMES.getValue())) {
                        String paramValue = param.getParamValue();
                        if (StringUtils.isNotBlank(paramValue)) {
                            result.setLimitTimes(Integer.parseInt(paramValue));
                        }
                    }
                    if (StringUtils.equals(param.getParamKey(), ParamConstants.BASIC.LOGIN_LIMIT_RELIEVETIMES.getValue())) {
                        String paramValue = param.getParamValue();
                        if (StringUtils.isNotBlank(paramValue)) {
                            result.setRelieveTimes(Integer.parseInt(paramValue));
                        }
                    }
                    if (StringUtils.equals(param.getParamKey(), ParamConstants.BASIC.LOGIN_LIMIT_OPEN.getValue())) {
                        boolean open = StringUtils.equals("true", param.getParamValue());
                        result.setOpen(open ? "true" : "false");
                    }
                }

            }
        }
        return result;
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

    @Transactional
    public CasSaveResult editBasic(List<SystemParameter> parameters) {
        CasSaveResult casSaveResult = afterSwitchDefaultLogin(parameters);
        BasicInfo basicInfo = basicInfo();
        for (int i = 0; i < parameters.size(); i++) {
            SystemParameter parameter = parameters.get(i);
            SystemParameterExample example = new SystemParameterExample();

            example.createCriteria().andParamKeyEqualTo(parameter.getParamKey());
            if (systemParameterMapper.countByExample(example) > 0) {
                systemParameterMapper.updateByPrimaryKey(parameter);
            } else {
                systemParameterMapper.insert(parameter);
            }
            example.clear();
        }
        datasourceService.updateDatasourceStatusJob(basicInfo, parameters);
        return casSaveResult;
    }


    @Transactional
    public void resetCas() {
        Map<String, CasXpackService> beansOfType = SpringContextUtil.getApplicationContext().getBeansOfType((CasXpackService.class));
        if (beansOfType.keySet().size() == 0) DEException.throwException("当前未启用CAS");
        CasXpackService casXpackService = SpringContextUtil.getBean(CasXpackService.class);
        if (ObjectUtils.isEmpty(casXpackService)) DEException.throwException("当前未启用CAS");

        String loginTypePk = "basic.loginType";
        SystemParameter loginTypeParameter = systemParameterMapper.selectByPrimaryKey(loginTypePk);
        if (ObjectUtils.isNotEmpty(loginTypeParameter) && StringUtils.equals("3", loginTypeParameter.getParamValue())) {
            loginTypeParameter.setParamValue("0");
            systemParameterMapper.updateByPrimaryKeySelective(loginTypeParameter);
        }
        casXpackService.setEnabled(false);
    }

    public CasSaveResult afterSwitchDefaultLogin(List<SystemParameter> parameters) {
        CasSaveResult casSaveResult = new CasSaveResult();
        casSaveResult.setNeedLogout(false);
        Map<String, CasXpackService> beansOfType = SpringContextUtil.getApplicationContext().getBeansOfType((CasXpackService.class));
        if (beansOfType.keySet().size() == 0) return casSaveResult;
        CasXpackService casXpackService = SpringContextUtil.getBean(CasXpackService.class);
        if (ObjectUtils.isEmpty(casXpackService)) return casSaveResult;

        AtomicReference<String> loginType = new AtomicReference();
        boolean containLoginType = parameters.stream().anyMatch(param -> {
            if (StringUtils.equals(param.getParamKey(), LOGIN_TYPE_KEY)) {
                loginType.set(param.getParamValue());
                return true;
            }
            return false;
        });
        if (!containLoginType) return casSaveResult;


        SystemParameter systemParameter = systemParameterMapper.selectByPrimaryKey(LOGIN_TYPE_KEY);
        String originVal = null;
        if (ObjectUtils.isNotEmpty(systemParameter)) {
            originVal = systemParameter.getParamValue();
        }

        if (StringUtils.equals(originVal, loginType.get())) return casSaveResult;

        if (StringUtils.equals(CAS_LOGIN_TYPE, loginType.get())) {
            casSaveResult.setNeedLogout(true);
            casXpackService.setEnabled(true);
            casSaveResult.setCasEnable(true);
        }

        if (StringUtils.equals(CAS_LOGIN_TYPE, originVal)) {
            casSaveResult.setNeedLogout(true);
            casXpackService.setEnabled(false);
            casSaveResult.setCasEnable(false);
        }
        return casSaveResult;
    }

    public List<SystemParameter> getParamList(String type) {
        SystemParameterExample example = new SystemParameterExample();
        example.createCriteria().andParamKeyLike(type + "%");
        return systemParameterMapper.selectByExample(example);
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

    public Integer defaultLoginType() {
        String value = getValue(LOGIN_TYPE_KEY);
        return StringUtils.isNotBlank(value) ? Integer.parseInt(value) : 0;
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
            if (systemParameter.getType().equalsIgnoreCase("blob")) {
                Map<String, DisplayXpackService> beansOfType = SpringContextUtil.getApplicationContext().getBeansOfType((DisplayXpackService.class));
                DisplayXpackService displayXpackService = null;
                if (beansOfType.keySet().size() > 0 && (displayXpackService = SpringContextUtil.getBean(DisplayXpackService.class)) != null) {
                    String paramValue = systemParameter.getParamValue();
                    if (StringUtils.isNotBlank(paramValue)) {
                        long blobId = Long.parseLong(paramValue);
                        String content = displayXpackService.readBlob(blobId);
                        systemParameterDTO.setParamValue(content);
                    }
                } else {
                    systemParameterDTO.setParamValue(null);
                }
            }
            dtoList.add(systemParameterDTO);
        }
        dtoList.sort(Comparator.comparingInt(SystemParameter::getSort));
        return dtoList;
    }

    public void saveUIInfo(Map<String, List<SystemParameterDTO>> request, List<MultipartFile> bodyFiles)
            throws IOException {
        List<SystemParameterDTO> parameters = request.get("systemParams");
        if (null != bodyFiles)
            for (MultipartFile multipartFile : bodyFiles) {
                if (!multipartFile.isEmpty()) {
                    // 防止添加非图片文件
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
                    parameters.stream()
                            .filter(systemParameterDTO -> systemParameterDTO.getParamKey().equalsIgnoreCase(split[1]))
                            .forEach(systemParameterDTO -> {
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
                    FileMetadata fileMetadata = fileService.saveFile(systemParameter.getFile(),
                            systemParameter.getFileName());
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

    public BasicInfo templateMarketInfo() {
        BasicInfo basicInfo = new BasicInfo();
        List<SystemParameter> result = this.getParamList("basic.template");
        if (CollectionUtils.isNotEmpty(result)) {
            result.stream().forEach(param -> {
                if (StringUtils.equals(param.getParamKey(), ParamConstants.BASIC.TEMPLATE_MARKET_ULR.getValue())) {
                    basicInfo.setTemplateMarketUlr(param.getParamValue());
                }
                if (StringUtils.equals(param.getParamKey(), ParamConstants.BASIC.TEMPLATE_ACCESS_KEY.getValue())) {
                    basicInfo.setTemplateAccessKey(param.getParamValue());
                }
            });
        }
        if (StringUtils.isEmpty(basicInfo.getTemplateMarketUlr()) || StringUtils.isEmpty(basicInfo.getTemplateAccessKey())) {
            DataEaseException.throwException("Please check market setting info");
        }
        return basicInfo;
    }

}
