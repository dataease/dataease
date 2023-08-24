package io.dataease.service;

import io.dataease.plugins.common.base.domain.SystemParameter;
import io.dataease.plugins.common.base.domain.SystemParameterExample;
import io.dataease.plugins.common.base.mapper.SystemParameterMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@Service
public class BaseDisplayService {
    @Resource
    private SystemParameterMapper systemParameterMapper;
    @Resource
    private FileService fileService;

    public List<SystemParameter> getParamList(String type) {
        SystemParameterExample example = new SystemParameterExample();
        example.createCriteria().andParamKeyLike(type + "%");
        return systemParameterMapper.selectByExample(example);
    }

    public byte[] loadFileAsBytes(String fileId) {
        return fileService.loadFileAsBytes(fileId);
    }

    public ResponseEntity<byte[]> getImage(String imageName) throws IOException {
        byte[] bytes = null;
        List<SystemParameter> paramList = getParamList("ui." + imageName);
        if (!CollectionUtils.isEmpty(paramList)) {
            SystemParameter sp = paramList.get(0);
            String paramValue = sp.getParamValue();
            if (StringUtils.isNotBlank(paramValue)) {
                bytes = loadFileAsBytes(paramValue);
            }
        }

        MediaType contentType = MediaType.parseMediaType("application/octet-stream");
        if (bytes == null) {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
            switch (imageName) {
                case "favicon":
                    bytes = IOUtils.toByteArray(resolver.getResources("/static/img/favicon.ico")[0].getInputStream());
                    contentType = MediaType.valueOf("image/vnd.microsoft.icon");
                    break;
                case "logo":
                    bytes = IOUtils.toByteArray(resolver.getResources("/static/img/logo-light-MeterSphere*.svg")[0].getInputStream());
                    contentType = MediaType.valueOf("image/svg+xml");
                    break;
                case "loginImage":
                    bytes = IOUtils.toByteArray(resolver.getResources("/static/img/info*.png")[0].getInputStream());
                    break;
                case "loginLogo":
                    bytes = IOUtils.toByteArray(resolver.getResources("/static/img/logo-dark-MeterSphere*.svg")[0].getInputStream());
                    contentType = MediaType.valueOf("image/svg+xml");
                    break;
                default:
                    break;
            }
        }

        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + imageName + "\"")
                .body(bytes);
    }
}
