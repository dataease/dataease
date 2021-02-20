package io.dataease.commons.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Set;

public class XMLUtils {

    private static void jsonToXmlStr(JSONObject jObj, StringBuffer buffer) {
        Set<Map.Entry<String, Object>> se = jObj.entrySet();
        for (Map.Entry<String, Object> en : se) {
            if ("com.alibaba.fastjson.JSONObject".equals(en.getValue().getClass().getName())) {
                buffer.append("<").append(en.getKey()).append(">");
                JSONObject jo = jObj.getJSONObject(en.getKey());
                jsonToXmlStr(jo, buffer);
                buffer.append("</").append(en.getKey()).append(">");
            } else if ("com.alibaba.fastjson.JSONArray".equals(en.getValue().getClass().getName())) {
                JSONArray jarray = jObj.getJSONArray(en.getKey());
                for (int i = 0; i < jarray.size(); i++) {
                    buffer.append("<").append(en.getKey()).append(">");
                    if (StringUtils.isNotBlank(jarray.getString(i))) {
                        JSONObject jsonobject = jarray.getJSONObject(i);
                        jsonToXmlStr(jsonobject, buffer);
                        buffer.append("</").append(en.getKey()).append(">");
                    }
                }
            } else if ("java.lang.String".equals(en.getValue().getClass().getName())) {
                buffer.append("<").append(en.getKey()).append(">").append(en.getValue());
                buffer.append("</").append(en.getKey()).append(">");
            }
        }
    }

    public static String jsonToXmlStr(JSONObject jObj) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        try {
            jsonToXmlStr(jObj, buffer);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
        return buffer.toString();
    }
}
