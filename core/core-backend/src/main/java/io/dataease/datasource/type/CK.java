package io.dataease.datasource.type;

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
import java.util.regex.Pattern;

@Data
@Component("ck")
public class CK extends DatasourceConfiguration {
    private String driver = "com.clickhouse.jdbc.ClickHouseDriver";
    private String extraParams = "";
    private String compressAlgorithm = "none"; // 默认设置为none以避免HTTP压缩问题
    private String sslCA;
    private String sslCert;
    private String sslKey;

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
        try {
            Path certDir = Paths.get(System.getProperty("java.io.tmpdir"), "dataease2", "clickhouse-ssl");
            Files.createDirectories(certDir);
            Path certFile = Files.createTempFile(certDir, "de-" + prefix + "-", ".pem");
            Files.writeString(certFile, certContent, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
            certFile.toFile().deleteOnExit();
            return certFile.toAbsolutePath().toString();
        } catch (IOException e) {
            DEException.throwException("SSL cert write failed: " + e.getMessage());
            return null;
        }
    }

    private String appendParam(String jdbcUrl, String paramName, String paramValue) {
        String join = jdbcUrl.contains("?") ? "&" : "?";
        return jdbcUrl + join + paramName + "=" + paramValue;
    }

    private boolean containsParam(String jdbcUrl, String paramName) {
        return Pattern.compile("(?i)([?&])" + Pattern.quote(paramName) + "=").matcher(jdbcUrl).find();
    }
}
