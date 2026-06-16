package io.dataease.share.manage;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.exception.DEException;
import io.dataease.share.dao.auto.entity.XpackShare;
import io.dataease.share.dao.auto.mapper.XpackShareRepository;
import io.dataease.utils.LogUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Predicate;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("shareSecretManage")
public class ShareSecretManage {

    private static final String SECRET_KEY = "linkPwd";

    @Getter
    private String defaultPwd;

    @Value("${dataease.default-link-pwd:}")
    private String configuredPwd;

    @Value("${dataease.path.share-secret:/opt/dataease2.0/conf/share-secret.json}")
    private String secretFilePath;

    @Resource
    private XpackShareRepository xpackShareRepository;

    @PostConstruct
    public void init() {
        if (StringUtils.isNotBlank(configuredPwd)) {
            defaultPwd = configuredPwd;
            return;
        }
        defaultPwd = loadOrGenerateSecret();
    }

    private String loadOrGenerateSecret() {
        ObjectMapper objectMapper = new ObjectMapper();
        File jsonFile;
        try {
            jsonFile = new File(secretFilePath).getCanonicalFile();
        } catch (IOException e) {
            LogUtil.error("Invalid share-secret file path: " + secretFilePath);
            return generateSecret();
        }
        Map<String, Object> config = new HashMap<>();
        if (jsonFile.exists()) {
            try {
                config = objectMapper.readValue(jsonFile, Map.class);
            } catch (IOException e) {
                LogUtil.warn("Failed to read share-secret file, will regenerate: " + e.getMessage());
            }
        }
        Object secretValue = config.get(SECRET_KEY);
        if (secretValue != null) {
            return secretValue.toString();
        }
        String generated = generateSecret();
        config.put(SECRET_KEY, generated);
        try {
            File parentDir = jsonFile.getParentFile();
            if (parentDir != null) {
                parentDir.mkdirs();
            }
            try (FileOutputStream fos = new FileOutputStream(jsonFile)) {
                objectMapper.writeValue(fos, config);
            }
            jsonFile.setReadable(true, true);
            jsonFile.setWritable(true, true);
            jsonFile.setExecutable(false, false);
            LogUtil.info("Generated share link secret saved to " + jsonFile.getAbsolutePath() + ". Please keep this file secure.");
        } catch (IOException e) {
            LogUtil.error("Failed to persist share-secret file: " + e.getMessage());
        }
        return generated;
    }

    private String generateSecret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    public String getSecret(Long resourceId, Long uid) {
        Specification<XpackShare> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("creator"), uid));
            predicates.add(cb.equal(root.get("resourceId"), resourceId));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        XpackShare xpackShare = xpackShareRepository.findOne(spec).orElse(null);
        if (ObjectUtils.isEmpty(xpackShare)) DEException.throwException("Share resource do not exist");
        String sharePwd = xpackShare.getPwd();
        return StringUtils.isNotBlank(sharePwd) ? sharePwd : defaultPwd;
    }
}
