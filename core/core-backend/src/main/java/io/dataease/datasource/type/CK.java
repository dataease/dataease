package io.dataease.datasource.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Data
@Component("ck")
public class CK extends DatasourceConfiguration {
    private static final Map<String, String> SSL_CERT_PATH_CACHE = new ConcurrentHashMap<>();
    private String driver = "com.clickhouse.jdbc.ClickHouseDriver";
    private String extraParams = "";
    private String compressAlgorithm = "none"; // 默认设置为none以避免HTTP压缩问题
    private String sslCA;
    private String sslCert;
    private String sslKey;

    @JsonIgnore
    private List<String> ILLEGAL_PARAMETERS = Arrays.asList("jndi:", "rmi:", "ldap:", "ldaps:", "dns:", "nis:", "corba:",
            "java.naming.factory.initial", "java.naming.provider.url");

    public String getJdbc() {
        String jdbcUrl;
        if (StringUtils.isNotEmpty(getUrlType()) && !getUrlType().equalsIgnoreCase("hostName")) {
            if (!getJdbcUrl().startsWith("jdbc:clickhouse")) {
                DEException.throwException("Illegal jdbcUrl: " + getJdbcUrl());
            }
            jdbcUrl = getJdbcUrl();
        } else {
            StringBuilder builder = new StringBuilder();
            if (StringUtils.isEmpty(extraParams.trim())) {
                builder.append("jdbc:clickhouse://")
                        .append(getLHost().trim())
                        .append(":")
                        .append(getLPort().toString().trim())
                        .append("/")
                        .append(getDataBase().trim());
            } else {
                builder.append("jdbc:clickhouse://")
                        .append(getLHost().trim())
                        .append(":")
                        .append(getLPort().toString().trim())
                        .append("/")
                        .append(getDataBase().trim())
                        .append("?")
                        .append(getExtraParams().trim());
            }
            jdbcUrl = builder.toString();
        }

        if (!containsParam(jdbcUrl, "compress_algorithm") && !containsParam(jdbcUrl, "enable_http_compression")) {
            jdbcUrl = appendParam(jdbcUrl, "compress_algorithm", compressAlgorithm);
        }
        if (StringUtils.isNotBlank(sslCA) || StringUtils.isNotBlank(sslCert) || StringUtils.isNotBlank(sslKey)) {
            if (!containsParam(jdbcUrl, "ssl")) {
                jdbcUrl = appendParam(jdbcUrl, "ssl", "true");
            }
            jdbcUrl = appendCertParam(jdbcUrl, "sslCA", sslCA, "ca");
            jdbcUrl = appendCertParam(jdbcUrl, "sslCert", sslCert, "cert");
            jdbcUrl = appendCertParam(jdbcUrl, "sslKey", sslKey, "key");
        }
        checkIllegalParameters(jdbcUrl);
        return jdbcUrl;
    }

    private String appendCertParam(String jdbcUrl, String paramName, String certContent, String filePrefix) {
        if (StringUtils.isBlank(certContent) || containsParam(jdbcUrl, paramName)) {
            return jdbcUrl;
        }
        String certPath = writeTempCert(certContent, filePrefix);
        return appendParam(jdbcUrl, paramName, certPath);
    }

    private String writeTempCert(String certContent, String prefix) {
        String cacheKey = prefix + ":" + sha256(certContent);
        String cachedPath = SSL_CERT_PATH_CACHE.get(cacheKey);
        if (StringUtils.isNotBlank(cachedPath) && Files.exists(Paths.get(cachedPath))) {
            return cachedPath;
        }
        try {
            Path certDir = Paths.get(System.getProperty("java.io.tmpdir"), "dataease2", "clickhouse-ssl");
            Files.createDirectories(certDir);
            Path certFile = Files.createTempFile(certDir, "de-" + prefix + "-", ".pem");
            Files.writeString(certFile, certContent, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
            certFile.toFile().deleteOnExit();
            String certPath = certFile.toAbsolutePath().toString();
            SSL_CERT_PATH_CACHE.put(cacheKey, certPath);
            return certPath;
        } catch (IOException e) {
            DEException.throwException("SSL cert write failed: " + e.getMessage());
            return null;
        }
    }

    private String sha256(String content) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                String h = Integer.toHexString(b & 0xff);
                if (h.length() == 1) {
                    hex.append('0');
                }
                hex.append(h);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            DEException.throwException("SHA-256 not supported: " + e.getMessage());
            return "";
        }
    }

    private String appendParam(String jdbcUrl, String paramName, String paramValue) {
        String join = jdbcUrl.contains("?") ? "&" : "?";
        return jdbcUrl + join + paramName + "=" + paramValue;
    }

    private boolean containsParam(String jdbcUrl, String paramName) {
        return Pattern.compile("(?i)([?&])" + Pattern.quote(paramName) + "=").matcher(jdbcUrl).find();
    }

    private void checkIllegalParameters(String jdbcUrl) {
        String lowerUrl = jdbcUrl.toLowerCase();
        for (String illegalParam : ILLEGAL_PARAMETERS) {
            if (lowerUrl.contains(illegalParam.toLowerCase())) {
                throw new SecurityException("Illegal parameter detected in JDBC URL: " + illegalParam);
            }
        }
    }

}
