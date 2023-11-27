package io.dataease.template.manage;

import io.dataease.api.template.dto.TemplateManageFileDTO;
import io.dataease.api.template.dto.TemplateMarketDTO;
import io.dataease.api.template.dto.TemplateMarketPreviewInfoDTO;
import io.dataease.api.template.response.*;
import io.dataease.api.template.vo.MarketApplicationSpecVO;
import io.dataease.api.template.vo.MarketMetaDataVO;
import io.dataease.api.template.vo.TemplateCategoryVO;
import io.dataease.exception.DEException;
import io.dataease.operation.manage.CoreOptRecentManage;
import io.dataease.system.manage.SysParameterManage;
import io.dataease.utils.HttpClientConfig;
import io.dataease.utils.HttpClientUtil;
import io.dataease.utils.JsonUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: wangjiahao
 */
@Service
public class TemplateMarketManage {

    private final static String POSTS_API = "/api/content/posts?page=0&size=2000";

    private final static String POSTS_API_V2 = "/apis/api.store.halo.run/v1alpha1/applications?keyword=&priceMode=&sort=latestReleaseTimestamp%2Cdesc&type=THEME&deVersion=V2&templateType=&label=&page=1&size=2000";
    private final static String CATEGORIES_API = "/api/content/categories";

    private final static String TEMPLATE_META_DATA_URL = "/upload/meta_data.json";

    @Resource
    private SysParameterManage sysParameterManage;

    @Resource
    private CoreOptRecentManage coreOptRecentManage;

    /**
     * @param templateUrl template url
     * @Description Get template file from template market
     */
    public TemplateManageFileDTO getTemplateFromMarket(String templateUrl) {
        if (StringUtils.isNotEmpty(templateUrl)) {
            String sufUrl = sysParameterManage.groupVal("template.").get("template.url");
            String templateInfo = HttpClientUtil.get(sufUrl + templateUrl, null);
            return JsonUtil.parseObject(templateInfo, TemplateManageFileDTO.class);
        } else {
            return null;
        }
    }

    /**
     * @param url content api url
     * @Description Get info from template market content api
     */
    public String marketGet(String url, String accessKey) {
        HttpClientConfig config = new HttpClientConfig();
        config.addHeader("API-Authorization", accessKey);
        return HttpClientUtil.get(url, config);
    }

     private MarketTemplateV2BaseResponse templateQuery(Map<String, String>  templateParams){
         String result = marketGet(templateParams.get("template.url") + POSTS_API_V2, null);
         MarketTemplateV2BaseResponse postsResult = JsonUtil.parseObject(result, MarketTemplateV2BaseResponse.class);
         return postsResult;
     }

    public MarketBaseResponse searchTemplate() {
        try {
            Map<String, String> templateParams = sysParameterManage.groupVal("template.");
            return baseResponseV2Trans(templateQuery(templateParams), templateParams.get("template.url"));
        } catch (Exception e) {
            DEException.throwException(e);
        }
        return null;
    }

    public MarketBaseResponse searchTemplateRecommend() {
        try {
            Map<String, String> templateParams = sysParameterManage.groupVal("template.");
            return baseResponseV2TransRecommend(templateQuery(templateParams), templateParams.get("template.url"));
        } catch (Exception e) {
            DEException.throwException(e);
        }
        return null;
    }

    public MarketPreviewBaseResponse searchTemplatePreview() {
        try {
            Map<String, String> templateParams = sysParameterManage.groupVal("template.");
            return basePreviewResponseV2Trans(templateQuery(templateParams), templateParams.get("template.url"));
        } catch (Exception e) {
            DEException.throwException(e);
        }
        return null;
    }

    private MarketBaseResponse baseResponseV2TransRecommend(MarketTemplateV2BaseResponse v2BaseResponse, String url) {
        Map<String, Long> useTime = coreOptRecentManage.findTemplateRecentUseTime();
        Map<String, String> categoriesMap = getCategoriesBaseV2();
        List<TemplateMarketDTO> contents = new ArrayList<>();
        v2BaseResponse.getItems().stream().forEach(marketTemplateV2ItemResult -> {
            MarketApplicationSpecVO spec = marketTemplateV2ItemResult.getApplication().getSpec();
            if("Y".equalsIgnoreCase(spec.getSuggest())){
                contents.add(new TemplateMarketDTO(spec.getReadmeName(), spec.getDisplayName(), spec.getScreenshots().get(0).getUrl(), spec.getLinks().get(0).getUrl(), categoriesMap.get(spec.getLabel()), spec.getTemplateType(), useTime.get(spec.getReadmeName()),"Y"));
            }
        });
        // 最近使用排序
        Collections.sort(contents);
        return new MarketBaseResponse(url,contents);
    }

    private MarketBaseResponse baseResponseV2Trans(MarketTemplateV2BaseResponse v2BaseResponse, String url) {
        Map<String, Long> useTime = coreOptRecentManage.findTemplateRecentUseTime();
        Map<String, String> categoriesMap = getCategoriesBaseV2();
        List<TemplateMarketDTO> contents = new ArrayList<>();
        v2BaseResponse.getItems().stream().forEach(marketTemplateV2ItemResult -> {
            MarketApplicationSpecVO spec = marketTemplateV2ItemResult.getApplication().getSpec();
            contents.add(new TemplateMarketDTO(spec.getReadmeName(), spec.getDisplayName(), spec.getScreenshots().get(0).getUrl(), spec.getLinks().get(0).getUrl(), categoriesMap.get(spec.getLabel()), spec.getTemplateType(), useTime.get(spec.getReadmeName()),spec.getSuggest()));
        });
        // 最近使用排序
        Collections.sort(contents);
        return new MarketBaseResponse(url,contents);
    }

    private MarketPreviewBaseResponse basePreviewResponseV2Trans(MarketTemplateV2BaseResponse v2BaseResponse, String url) {
        Map<String, Long> useTime = coreOptRecentManage.findTemplateRecentUseTime();
        Map<String, String> categoriesMap = getCategoriesBaseV2();
        List<String> categories = new ArrayList<>();
        List<TemplateMarketPreviewInfoDTO> result = new ArrayList<>();
        categoriesMap.forEach((key,value)->{
            if(!"全部".equalsIgnoreCase(value)){
                categories.add(value);
                List<TemplateMarketDTO> contents = new ArrayList<>();
                v2BaseResponse.getItems().stream().forEach(marketTemplateV2ItemResult -> {
                    MarketApplicationSpecVO spec = marketTemplateV2ItemResult.getApplication().getSpec();
                    if(key.equalsIgnoreCase(spec.getLabel())){
                        contents.add(new TemplateMarketDTO(spec.getReadmeName(), spec.getDisplayName(), spec.getScreenshots().get(0).getUrl(), spec.getLinks().get(0).getUrl(), categoriesMap.get(spec.getLabel()), spec.getTemplateType(), useTime.get(spec.getReadmeName()),spec.getSuggest()));
                    }
                });
                Collections.sort(contents);
                result.add(new TemplateMarketPreviewInfoDTO(value,contents));
            }
        });
        // 最近使用排序
        return new MarketPreviewBaseResponse(url,categories,result);
    }

    public MarketBaseResponse searchTemplateV1() {
        try {
            Map<String, String> templateParams = sysParameterManage.groupVal("template.");
            String result = marketGet(templateParams.get("template.url") + POSTS_API, templateParams.get("template.accessKey"));
            MarketTemplateBaseResponse postsResult = JsonUtil.parseObject(result, MarketTemplateBaseResponse.class);
            MarketBaseResponse response = new MarketBaseResponse(templateParams.get("template.url"), postsResult.getData().getContent());
            return response;
        } catch (Exception e) {
            DEException.throwException(e);
        }
        return null;
    }

    public List<String> getCategoriesV1() {
        Map<String, String> templateParams = sysParameterManage.groupVal("template.");
        String resultStr = marketGet(templateParams.get("template.url") + CATEGORIES_API, templateParams.get("template.accessKey"));
        MarketCategoryBaseResponse categoryBaseResponse = JsonUtil.parseObject(resultStr, MarketCategoryBaseResponse.class);
        List<TemplateCategoryVO> categories = categoryBaseResponse.getData();
        if (CollectionUtils.isNotEmpty(categories)) {
            return categories.stream().filter(item -> !"应用系列".equals(item.getName())).sorted(Comparator.comparing(TemplateCategoryVO::getPriority)).map(TemplateCategoryVO::getName).collect(Collectors.toList());
        } else {
            return null;
        }

    }

    public List<String> getCategories() {
        return getCategoriesV2().stream().map(MarketMetaDataVO::getLabel)
                .collect(Collectors.toList());
    }

    public List<MarketMetaDataVO> getCategoriesObject() {
        List<MarketMetaDataVO> result = getCategoriesV2();
        result.add(0, new MarketMetaDataVO("suggest", "推荐"));
        result.add(0, new MarketMetaDataVO("recent", "最近使用"));
        return result;
    }

    public Map<String, String> getCategoriesBaseV2() {
        Map<String, String> categories = getCategoriesV2().stream()
                .collect(Collectors.toMap(MarketMetaDataVO::getSlug, MarketMetaDataVO::getLabel));
        return categories;
    }

    public List<MarketMetaDataVO> getCategoriesV2() {
        Map<String, String> templateParams = sysParameterManage.groupVal("template.");
        String resultStr = marketGet(templateParams.get("template.url") + TEMPLATE_META_DATA_URL, null);
        MarketMetaDataBaseResponse metaData = JsonUtil.parseObject(resultStr, MarketMetaDataBaseResponse.class);
        List<MarketMetaDataVO> categories = metaData.getLabels();
        return categories;
    }
}
