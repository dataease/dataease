package io.dataease.iam.utils;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
@Slf4j
public class HutoolUtil {
    @Value("${yisa.url}")
    private String url;

    public JSONObject postJson(JSONObject param, String apiUrl) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("code",1);
        String result = HttpUtil.post(url+apiUrl, param.toString());

        if (StringUtils.isBlank(result)) {
            return jsonObject;
        }

        //解析json字符串成JSON对象
        jsonObject = JSONUtil.parseObj(result);

        log.info("postJson apiUrl  msg:{}",jsonObject.getStr("msg"));
        return jsonObject;
    }

    public JSONObject postXwForm(Long id, String apiUrl) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("code",1);

        // 构造表单参数
        HttpRequest request = HttpRequest.post(url+apiUrl)
                .form("id", id);

        // 设置Content-Type为application/x-www-form-urlencoded
        request.contentType("application/x-www-form-urlencoded");

        String result =  request.execute().body();

        if (StringUtils.isBlank(result)) {
            return jsonObject;
        }

        //解析json字符串成JSON对象
        jsonObject = JSONUtil.parseObj(result);

        log.info("postJson apiUrl  msg:{}",jsonObject.getStr("msg"));
        return jsonObject;

    }
}
