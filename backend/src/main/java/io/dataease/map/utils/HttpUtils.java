package io.dataease.map.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import io.dataease.map.dto.request.MapRequest;
import io.dataease.map.dto.response.MapResponse;


import java.util.Map;

public class HttpUtils {

    private static final String url = "https://restapi.amap.com/v3/config/district";


    private static final String key = "a5d10d5d05a3a0868cec67c4d66cf025";
    private static final String extensions = "all";
    private static final Integer subdistrict = 0;



    public static MapResponse get(MapRequest request){
        request.setKey(key);
        request.setExtensions(extensions);
        request.setSubdistrict(subdistrict);
        Map<String, Object> param = BeanUtil.beanToMap(request);

        String s = HttpUtil.get(url, param);
        MapResponse mapResponse = JSONUtil.toBean(s, MapResponse.class);
        return mapResponse;
    }

    public static MapResponse get(String code) {
        MapRequest request = MapRequest.builder().keywords(code).build();
        return get(request);
    }


}
