package io.dataease.template.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.visualization.request.DataVisualizationBaseRequest;
import io.dataease.license.utils.LogUtil;
import io.dataease.template.dao.auto.entity.DeTemplateVersion;
import io.dataease.template.dao.auto.mapper.DeTemplateVersionMapper;
import io.dataease.utils.JsonUtil;
import io.dataease.visualization.server.StaticResourceServer;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.repository.init.ResourceReader;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * @author : WangJiaHao
 * @date : 2024/5/7
 */
@Service
public class TemplateLocalParseManage {

    @Resource
    private StaticResourceServer staticResourceServer;

    @Resource
    private DeTemplateVersionMapper deTemplateVersionMapper;

    @Resource(type = ResourceLoader.class)
    private ResourceLoader resourceLoader;

    public void doInit() throws Exception {
        org.springframework.core.io.Resource[] templateFiles = getAllFilesInResourceDirectory("template");
        if (templateFiles != null && templateFiles.length > 0) {
            for (int i = 0; i < templateFiles.length; i++) {
                org.springframework.core.io.Resource templateFile = templateFiles[i];
                String templateName = templateFile.getFilename();
                QueryWrapper<DeTemplateVersion> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("script", templateName);
                if (!deTemplateVersionMapper.exists(queryWrapper)) {
                    DeTemplateVersion version = new DeTemplateVersion();
                    version.setScript(templateName);
                    version.setInstalledOn(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
                    try {
                        String content = new String(templateFile.getInputStream().readAllBytes());;
                        DataVisualizationBaseRequest template = JsonUtil.parseObject(content, DataVisualizationBaseRequest.class);
                        parseCore(template);
                        version.setSuccess(true);
                        deTemplateVersionMapper.insert(version);
                    } catch (Exception e) {
                        LogUtil.error("De Template Version Error : " + templateName);
                        version.setSuccess(false);
                        deTemplateVersionMapper.insert(version);
                        break;
                    }
                }

            }
        }
    }

    public void parseCore(DataVisualizationBaseRequest template) {
        // 解析静态文件并保存
        staticResourceServer.saveFilesToServe(template.getStaticResource());
    }


    public org.springframework.core.io.Resource[] getAllFilesInResourceDirectory(String directoryName) throws Exception {
        // 创建一个 PathMatchingResourcePatternResolver 对象
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(resourceLoader);

        // 获取 classpath 下 template 目录下所有文件的 Resource 数组
        org.springframework.core.io.Resource[] resources = resolver.getResources("classpath:template/*");

        return resources;
    }

    public static String readFileContent(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (InputStream inputStream = Files.newInputStream(file.toPath());
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line);
            }
        }
        return content.toString();
    }

}
