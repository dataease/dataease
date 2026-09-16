package io.dataease.datasource.provider;

import io.dataease.datasource.type.StarRocks;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.TableField;
import io.dataease.utils.HttpClientConfig;
import io.dataease.utils.HttpClientUtil;
import io.dataease.utils.JsonUtil;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * StarRocks Stream Load 工具。
 * StarRocks 官方推荐通过 HTTP PUT 方式将本地数据流式导入，避免大批量 INSERT INTO。
 */
public class StarRocksStreamLoadUtil {

    private static final String STREAM_LOAD_PATH = "/api/%s/%s/_stream_load";
    private static final String COLUMN_SEPARATOR = "|~|";

    private StarRocksStreamLoadUtil() {
    }

    public static void streamLoad(StarRocks configuration, String tableName, List<String[]> dataList, List<TableField> tableFields) {
        if (configuration == null || StringUtils.isBlank(configuration.getHost())
                || StringUtils.isBlank(configuration.getDataBase())
                || configuration.getFePort() == null) {
            DEException.throwException("StarRocks Stream Load 配置不完整");
        }
        if (dataList == null || dataList.isEmpty()) {
            return;
        }

        String csv = buildCsv(dataList, tableFields);
        String streamLoadHost;
        Integer streamLoadPort;
        if (StringUtils.isNotBlank(configuration.getBeIp())) {
            if (configuration.getBePort() == null || configuration.getBePort() < 1 || configuration.getBePort() > 65535) {
                DEException.throwException("StarRocks BE HTTP 端口范围为1-65535");
            }
            streamLoadHost = configuration.getBeIp();
            streamLoadPort = configuration.getBePort();
        } else {
            if (configuration.getFePort() < 1 || configuration.getFePort() > 65535) {
                DEException.throwException("StarRocks FE HTTP 端口范围为1-65535");
            }
            streamLoadHost = configuration.getHost();
            streamLoadPort = configuration.getFePort();
        }
        String url = String.format("http://%s:%d" + STREAM_LOAD_PATH,
                streamLoadHost.trim(), streamLoadPort,
                configuration.getDataBase().trim(), tableName);

        HttpClientConfig httpClientConfig = new HttpClientConfig();
        httpClientConfig.addHeader("Expect", "100-continue");
        httpClientConfig.addHeader("format", "CSV");
        httpClientConfig.addHeader("column_separator", COLUMN_SEPARATOR);
        httpClientConfig.addHeader("max_filter_ratio", "1");
        httpClientConfig.addHeader("strict_mode", "false");
        if (StringUtils.isNotBlank(configuration.getUsername())) {
            String auth = Base64.getEncoder().encodeToString(
                    (configuration.getUsername() + ":" + StringUtils.defaultString(configuration.getPassword()))
                            .getBytes(StandardCharsets.UTF_8));
            httpClientConfig.addHeader("Authorization", "Basic " + auth);
        }
        String response = HttpClientUtil.putRawBody(url, "text/plain; charset=UTF-8", csv, false, httpClientConfig);
        checkResponse(response);
    }

    public static String buildCsv(List<String[]> dataList, List<TableField> tableFields) {
        List<TableField> checkedFields = tableFields.stream()
                .filter(TableField::isChecked)
                .toList();

        return dataList.stream()
                .map(row -> buildCsvRow(row, tableFields, checkedFields))
                .collect(Collectors.joining("\n")) + "\n";
    }

    private static String buildCsvRow(String[] row, List<TableField> tableFields, List<TableField> checkedFields) {
        StringBuilder builder = new StringBuilder();
        int checkedIndex = 0;
        for (int i = 0; i < tableFields.size(); i++) {
            if (!tableFields.get(i).isChecked()) {
                continue;
            }
            if (checkedIndex > 0) {
                builder.append(COLUMN_SEPARATOR);
            }
            String value = i < row.length ? row[i] : null;
            builder.append(escapeCsvValue(value));
            checkedIndex++;
        }
        return builder.toString();
    }

    private static String escapeCsvValue(String value) {
        if (value == null || value.isEmpty()) {
            return "\\N";
        }
        if (value.contains(COLUMN_SEPARATOR) || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static void checkResponse(String response) {
        if (StringUtils.isBlank(response)) {
            DEException.throwException("StarRocks Stream Load 返回为空");
        }
        Map<String, Object> result = JsonUtil.parseObject(response, Map.class);
        String status = result == null ? null : String.valueOf(result.get("Status"));
        if (!"Success".equalsIgnoreCase(status)) {
            String message = result == null ? null : String.valueOf(result.get("Message"));
            String errorUrl = result == null ? null : String.valueOf(result.get("ErrorURL"));
            StringBuilder error = new StringBuilder("StarRocks Stream Load 失败: ")
                    .append(StringUtils.defaultString(message, response));
            if (StringUtils.isNotBlank(errorUrl)) {
                error.append("，ErrorURL: ").append(errorUrl);
            }
            DEException.throwException(error.toString());
        }
    }
}
