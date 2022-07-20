package io.dataease.service.templateMarket;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import io.dataease.commons.utils.HttpClientConfig;
import io.dataease.commons.utils.HttpClientUtil;
import io.dataease.controller.request.templateMarket.TemplateMarketSearchRequest;
import io.dataease.controller.sys.response.BasicInfo;
import io.dataease.dto.panel.PanelTemplateFileDTO;
import io.dataease.dto.templateMarket.MarketBaseResponse;
import io.dataease.dto.templateMarket.TemplateCategory;
import io.dataease.dto.templateMarket.TemplateMarketDTO;
import io.dataease.exception.DataEaseException;
import io.dataease.service.system.SystemParameterService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Author: wangjiahao
 * Date: 2022/7/14
 * Description: ${userName}
 */
@Service
public class TemplateMarketService {

    private final static String POSTS_API="/api/content/posts?page=0&size=2000";
    private final static String CATEGORIES_API="/api/content/categories";

    @Resource
    private SystemParameterService systemParameterService;

    /**
     * @Description Get template file from template market
     * @param templateUrl template url
     */
    public PanelTemplateFileDTO getTemplateFromMarket(String templateUrl){
        if(StringUtils.isNotEmpty(templateUrl)){
            String sufUrl = systemParameterService.templateMarketInfo().getTemplateMarketUlr();
            Gson gson = new Gson();
            String templateInfo =  HttpClientUtil.get(sufUrl+templateUrl,null);
            return gson.fromJson(templateInfo, PanelTemplateFileDTO.class);
        }else{
            return null;
        }
    }

    /**
     * @Description Get info from template market content api
     * @param url content api url
     */
    public String marketGet(String url,String accessKey){
        HttpClientConfig config = new HttpClientConfig();
        config.addHeader("API-Authorization",accessKey);
        return  HttpClientUtil.get(url,config);
    }

    public MarketBaseResponse searchTemplate(TemplateMarketSearchRequest request){
        try{
            BasicInfo basicInfo = systemParameterService.templateMarketInfo();
            String result = marketGet(basicInfo.getTemplateMarketUlr()+POSTS_API,basicInfo.getTemplateAccessKey());
           List<TemplateMarketDTO> postsResult = JSONObject.parseObject(result).getJSONObject("data").getJSONArray("content").toJavaList(TemplateMarketDTO.class);
           return new MarketBaseResponse(basicInfo.getTemplateMarketUlr(),postsResult);
        }catch (Exception e){
            DataEaseException.throwException(e);
        }
        return null;
    }

    public List<String> getCategories(){
        BasicInfo basicInfo = systemParameterService.templateMarketInfo();
        String resultStr = marketGet(basicInfo.getTemplateMarketUlr()+CATEGORIES_API,basicInfo.getTemplateAccessKey());
        List<TemplateCategory> categories = JSONObject.parseObject(resultStr).getJSONArray("data").toJavaList(TemplateCategory.class);
        if(CollectionUtils.isNotEmpty(categories)){
            return categories.stream().map(TemplateCategory :: getName).collect(Collectors.toList());
        }else{
            return null;
        }

    }
}
