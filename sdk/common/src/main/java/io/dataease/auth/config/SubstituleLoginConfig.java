package io.dataease.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.utils.LogUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.UUID;

@ConditionalOnMissingBean(name = "loginServer")
@Configuration
public class SubstituleLoginConfig {
    private static final String PWD_KEY = "pwd";
    private static final String TOKEN_SECRET_KEY = "tokenSecret";

    @Value("${dataease.path.substitule:classpath:substitule.json}")
    private String jsonFilePath;

    private static String pwd;

    private static String tokenSecret;

    private static boolean ready = false;


    @ConditionalOnMissingBean(name = "loginServer")
    @Bean
    public Map<String, Object> substituleLoginData(ResourceLoader resourceLoader) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File jsonFile = new File(jsonFilePath);
        Map<String, Object> result = jsonFile.exists() ? objectMapper.readValue(jsonFile, Map.class) : new HashMap<>();
        boolean updated = false;
        Environment environment = CommonBeanFactory.getBean(Environment.class);
        String configuredPwd = environment == null ? null : environment.getProperty("dataease.default-pwd");
        pwd = readString(result, PWD_KEY);
        if (StringUtils.isBlank(pwd)) {
            pwd = StringUtils.isNotBlank(configuredPwd) ? configuredPwd : generatePwd();
            result.put(PWD_KEY, pwd);
            updated = true;
            if (StringUtils.isBlank(configuredPwd)) {
                LogUtil.info("Generated substitute admin password in " + jsonFilePath + ". Please keep the file secure.");
            }
        }
        tokenSecret = readString(result, TOKEN_SECRET_KEY);
        if (StringUtils.isBlank(tokenSecret)) {
            tokenSecret = generateSecret();
            result.put(TOKEN_SECRET_KEY, tokenSecret);
            updated = true;
        }
        if (updated) {
            writeConfig(jsonFile, result);
        }
        return result;
    }

    public static String getPwd() {
        loadSubstituleLoginData();
        return pwd;
    }

    public static String getTokenSecret() {
        loadSubstituleLoginData();
        return tokenSecret;
    }

    private static synchronized void loadSubstituleLoginData() {
        if (ready) {
            return;
        }
        ready = true;
        Object substituleLoginDataObject = CommonBeanFactory.getBean("substituleLoginData");
        if (substituleLoginDataObject != null) {
            Map<String, Object> substituleLoginData = (Map<String, Object>) substituleLoginDataObject;
            String configuredPwd = readString(substituleLoginData, PWD_KEY);
            if (StringUtils.isNotBlank(configuredPwd)) {
                pwd = configuredPwd;
            }
            String configuredTokenSecret = readString(substituleLoginData, TOKEN_SECRET_KEY);
            if (StringUtils.isNotBlank(configuredTokenSecret)) {
                tokenSecret = configuredTokenSecret;
            }
        }
    }

    public void modifyPwd(String pwd) {
        File file = new File(jsonFilePath);
        Map<String, String> myObject = new HashMap<>();
        myObject.put(PWD_KEY, pwd);
        myObject.put(TOKEN_SECRET_KEY, ObjectUtils.isNotEmpty(tokenSecret) ? tokenSecret : generateSecret());
        SubstituleLoginConfig.pwd = pwd;
        SubstituleLoginConfig.tokenSecret = myObject.get(TOKEN_SECRET_KEY);
        try {
            writeConfig(file, myObject);
        } catch (IOException e) {
            LogUtil.error(e.getCause(), new Throwable(e));
        }
    }

    private static void writeConfig(File file, Map<?, ?> config) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            mapper.writeValue(fos, config);
        }
    }

    private static String readString(Map<String, Object> config, String key) {
        Object value = config.get(key);
        return ObjectUtils.isEmpty(value) ? null : value.toString();
    }

    private static String generatePwd() {
        return "DE-" + UUID.randomUUID().toString().replace("-", "");
    }

    private static String generateSecret() {
        return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
    }
}
