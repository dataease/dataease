package io.dataease.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.utils.LogUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ConditionalOnMissingBean(name = "loginServer")
@Configuration
public class SubstituleLoginConfig {


    private static String jsonFilePath = "classpath:substitule.json";

    private static String pwd;

    private static boolean ready = false;


    @ConditionalOnMissingBean(name = "loginServer")
    @Bean
    public Map<String, Object> substituleLoginData(ResourceLoader resourceLoader) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File jsonFile = new File(jsonFilePath);
        if (!jsonFile.exists()) {
            pwd = CommonBeanFactory.getBean(Environment.class).getProperty("dataease.default-pwd", "DataEase@123456");
            modifyPwd(pwd);
        }
        return objectMapper.readValue(jsonFile, Map.class);
    }

    public static String getPwd() {
        if (!ready) {
            ready = true;
            Object substituleLoginDataObject = CommonBeanFactory.getBean("substituleLoginData");
            if (substituleLoginDataObject != null) {
                Map<String, Object> substituleLoginData = (Map<String, Object>) substituleLoginDataObject;
                if (ObjectUtils.isNotEmpty(substituleLoginData.get("pwd"))) {
                    pwd = substituleLoginData.get("pwd").toString();
                    return substituleLoginData.get("pwd").toString();
                }
            }
        }
        return pwd;
    }

    public static void modifyPwd(String pwd) {
        File file = new File(jsonFilePath);
        Map<String, String> myObject = new HashMap<>();
        myObject.put("pwd", pwd);
        SubstituleLoginConfig.pwd = pwd;
        ObjectMapper mapper = new ObjectMapper();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            // 将对象写入文件
            mapper.writeValue(fos, myObject);
        } catch (IOException e) {
            LogUtil.error(e.getCause(), new Throwable(e));
        }
    }
}
