package io.dataease.copilot.api;

import io.dataease.api.copilot.dto.ReceiveDTO;
import io.dataease.api.copilot.dto.SendDTO;
import io.dataease.copilot.dao.auto.entity.CoreCopilotConfig;
import io.dataease.copilot.dao.auto.mapper.CoreCopilotConfigMapper;
import io.dataease.exception.DEException;
import io.dataease.utils.HttpClientConfig;
import io.dataease.utils.HttpClientUtil;
import io.dataease.utils.JsonUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Map;

/**
 * @Author Junjun
 */
@Component
public class CopilotAPI {

    public static final String TOKEN = "/auth/token/license";

    public static final String FREE_TOKEN = "/auth/token/free";

    public static final String API = "/copilot/v1";

    public static final String CHART = "/generate-chart";

    public static final String RATE_LIMIT = "/rate-limit";

    @Resource
    private CoreCopilotConfigMapper coreCopilotConfigMapper;

    public String basicAuth(String userName, String password) {
        String auth = userName + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        return "Basic " + encodedAuth;
    }

    public String bearerAuth(String token) {
        return "Bearer " + token;
    }

    public CoreCopilotConfig getConfig() {
        CoreCopilotConfig coreCopilotConfig = coreCopilotConfigMapper.selectById(1);
        coreCopilotConfig.setPwd(new String(Base64.getDecoder().decode(coreCopilotConfig.getPwd())));
        return coreCopilotConfig;
    }

    public String getToken(String license) throws Exception {
        String url = getConfig().getCopilotUrl() + TOKEN;
        JSONObject json = new JSONObject();
        json.put("licenseText", license);
        HttpClientConfig httpClientConfig = new HttpClientConfig();
        String tokenJson = HttpClientUtil.post(url, json.toString(), httpClientConfig);
        return (String) JsonUtil.parse(tokenJson, Map.class).get("accessToken");
    }

    public String getFreeToken() throws Exception {
        String url = getConfig().getCopilotUrl() + FREE_TOKEN;
        HttpClientConfig httpClientConfig = new HttpClientConfig();
        String tokenJson = HttpClientUtil.post(url, "", httpClientConfig);
        return (String) JsonUtil.parse(tokenJson, Map.class).get("accessToken");
    }

    public ReceiveDTO generateChart(String token, SendDTO sendDTO) {
        String url = getConfig().getCopilotUrl() + API + CHART;
        String request = (String) JsonUtil.toJSONString(sendDTO);
        HttpClientConfig httpClientConfig = new HttpClientConfig();
        httpClientConfig.addHeader("Authorization", bearerAuth(token));
        String result = HttpClientUtil.post(url, request, httpClientConfig);
        return JsonUtil.parseObject(result, ReceiveDTO.class);
    }

    public void checkRateLimit(String token) {
        String url = getConfig().getCopilotUrl() + API + RATE_LIMIT;
        HttpClientConfig httpClientConfig = new HttpClientConfig();
        httpClientConfig.addHeader("Authorization", bearerAuth(token));
        HttpResponse httpResponse = HttpClientUtil.postWithHeaders(url, null, httpClientConfig);
        Header[] allHeaders = httpResponse.getAllHeaders();

        String limit = "";
        String seconds = "";
        for (Header header : allHeaders) {
            if (StringUtils.equalsIgnoreCase(header.getName(), "x-rate-limit-remaining")) {
                limit = header.getValue();
            }
            if (StringUtils.equalsIgnoreCase(header.getName(), "x-rate-limit-retry-after-seconds")) {
                seconds = header.getValue();
            }
        }
        if (Long.parseLong(limit) <= 0) {
            DEException.throwException(String.format("当前请求频率已达上限，请在%s秒后重试", seconds));
        }
    }
}
