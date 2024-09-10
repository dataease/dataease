package io.dataease.template.manage;

import io.dataease.api.template.dto.TemplateManageDTO;
import io.dataease.api.template.dto.TemplateManageFileDTO;
import io.dataease.api.template.dto.TemplateMarketDTO;
import io.dataease.api.template.dto.TemplateMarketPreviewInfoDTO;
import io.dataease.api.template.response.*;
import io.dataease.api.template.vo.MarketApplicationMetaDataVO;
import io.dataease.api.template.vo.MarketApplicationSpecVO;
import io.dataease.api.template.vo.MarketLatestReleaseVO;
import io.dataease.api.template.vo.MarketMetaDataVO;
import io.dataease.constant.CommonConstants;
import io.dataease.exception.DEException;
import io.dataease.operation.manage.CoreOptRecentManage;
import io.dataease.system.manage.SysParameterManage;
import io.dataease.template.dao.auto.entity.VisualizationTemplateCategoryMap;
import io.dataease.template.dao.auto.mapper.VisualizationTemplateCategoryMapMapper;
import io.dataease.template.dao.ext.ExtVisualizationTemplateMapper;
import io.dataease.utils.HttpClientConfig;
import io.dataease.utils.HttpClientUtil;
import io.dataease.utils.JsonUtil;
import io.dataease.utils.LogUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Author: wangjiahao
 */
@Service
public class TemplateCenterManage {
    private final static String POSTS_API_V2 = "/apis/api.store.halo.run/v1alpha1/applications?keyword=&priceMode=&sort=latestReleaseTimestamp%2Cdesc&type=THEME&deVersion=V2&templateType=&label=&page=1&size=2000";
    private final static String TEMPLATE_META_DATA_URL = "/upload/meta_data.json";
    private final static String TEMPLATE_BASE_INFO_URL = "/apis/api.store.halo.run/v1alpha1/applications/";
    @Resource
    private SysParameterManage sysParameterManage;

    @Resource
    private CoreOptRecentManage coreOptRecentManage;

    @Resource
    private ExtVisualizationTemplateMapper templateManageMapper;

    @Resource
    private VisualizationTemplateCategoryMapMapper categoryMapMapper;

    /**
     * @param templateUrl template url
     * @Description Get template file from template market
     */
    public TemplateManageFileDTO getTemplateFromMarket(String templateUrl) {
        if (StringUtils.isNotEmpty(templateUrl)) {
            String templateName = templateUrl.substring(templateUrl.lastIndexOf("/") + 1, templateUrl.length());
            templateUrl = templateUrl.replace(templateName, URLEncoder.encode(templateName, StandardCharsets.UTF_8).replace("+", "%20"));
            String sufUrl = sysParameterManage.groupVal("template.").get("template.url");
            String templateInfo = HttpClientUtil.get(sufUrl + templateUrl, null);
            return JsonUtil.parseObject(templateInfo, TemplateManageFileDTO.class);
        } else {
            return null;
        }
    }

    /**
     * @param templateUrl template url
     * @Description Get template file from template market
     */
    public TemplateManageFileDTO getTemplateFromMarketV2(String templateName) {
        if (StringUtils.isNotEmpty(templateName)) {
            String sufUrl = sysParameterManage.groupVal("template.").get("template.url");
            String templateBaseInfo = HttpClientUtil.get(sufUrl + TEMPLATE_BASE_INFO_URL + templateName, null);
            MarketTemplateV2ItemResult baseItemInfo = JsonUtil.parseObject(templateBaseInfo, MarketTemplateV2ItemResult.class);
            String templateUrl = sufUrl + "/store/apps/" + templateName +
                    "/releases/download/" + baseItemInfo.getLatestRelease().getRelease().getMetadata().getName()
                    + "/assets/" + baseItemInfo.getLatestRelease().getAssets().getFirst().getMetadata().getName();
            String templateInfo = HttpClientUtil.get(templateUrl, null);
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
        config.setConnectTimeout(5000);
        config.setSocketTimeout(10000);
        config.setConnectionRequestTimeout(5000);
        return HttpClientUtil.get(url, config);
    }

    private MarketTemplateV2BaseResponse templateQuery(Map<String, String> templateParams) {
        try {
            String result = marketGet(templateParams.get("template.url") + POSTS_API_V2, null);
            MarketTemplateV2BaseResponse postsResult = JsonUtil.parseObject(result, MarketTemplateV2BaseResponse.class);
            return postsResult;
        } catch (Exception e) {
            LogUtil.error(e);
            return null;
        }
    }

    public MarketBaseResponse searchTemplate() {
        try {
            Map<String, String> templateParams = sysParameterManage.groupVal("template.");
            return baseResponseV2Trans(templateQuery(templateParams), searchTemplateFromManage(), templateParams.get("template.url"));
        } catch (Exception e) {
            LogUtil.error(e);
            e.printStackTrace();
        }
        return null;
    }

    private List<TemplateMarketDTO> searchTemplateFromManage() {
        try {
            List<TemplateManageDTO> manageResult = templateManageMapper.findBaseTemplateList();
            List<TemplateManageDTO> categories = templateManageMapper.findCategories(null);
            Map<String, String> categoryMap = categories.stream()
                    .collect(Collectors.toMap(TemplateManageDTO::getId, TemplateManageDTO::getName));
            return baseManage2MarketTrans(manageResult, categoryMap);
        } catch (Exception e) {
            DEException.throwException(e);
        }
        return null;
    }

    private List<TemplateMarketDTO> baseManage2MarketTrans(List<TemplateManageDTO> manageResult, Map<String, String> categoryMap) {
        List<TemplateMarketDTO> result = new ArrayList<>();
        manageResult.stream().forEach(templateManageDTO -> {
            templateManageDTO.setCategoryName(categoryMap.get(templateManageDTO.getPid()));
            List<String> categories = templateManageDTO.getCategories();
            if (!CollectionUtils.isEmpty(categories)) {
                List<String> categoryNames = categories.stream().map(categoryId -> categoryMap.get(categoryId)).collect(Collectors.toList());
                templateManageDTO.setCategoryNames(categoryNames);
                result.add(new TemplateMarketDTO(templateManageDTO));
            }
        });
        return result;
    }


    public MarketBaseResponse searchTemplateRecommend() {
        MarketTemplateV2BaseResponse v2BaseResponse = null;
        Map<String, String> templateParams = sysParameterManage.groupVal("template.");
        // 模版市场推荐
        try {
            v2BaseResponse = templateQuery(templateParams);
        } catch (Exception e) {
            DEException.throwException(e);
        }
        // 模版管理使用次数推荐
        List<TemplateMarketDTO> manage = searchTemplateFromManage();
        return baseResponseV2TransRecommend(v2BaseResponse, manage, templateParams.get("template.url"));
    }

    public MarketPreviewBaseResponse searchTemplatePreview() {
        try {
            MarketBaseResponse baseContentRsp = searchTemplate();
            List<MarketMetaDataVO> categories = baseContentRsp.getCategories().stream().filter(category -> !"最近使用".equals(category.getLabel())).collect(Collectors.toList());
            List<TemplateMarketDTO> contents = baseContentRsp.getContents();
            List<TemplateMarketPreviewInfoDTO> previewContents = new ArrayList<>();
            categories.forEach(category -> {
                if ("推荐".equals(category.getLabel())) {
                    previewContents.add(new TemplateMarketPreviewInfoDTO(category, contents.stream().filter(template -> "Y".equals(template.getSuggest())).collect(Collectors.toList())));
                } else {
                    previewContents.add(new TemplateMarketPreviewInfoDTO(category, contents.stream().filter(template -> checkCategoryMatch(template, category.getLabel())).collect(Collectors.toList())));
                }
            });
            return new MarketPreviewBaseResponse(baseContentRsp.getBaseUrl(), categories.stream().map(MarketMetaDataVO::getLabel)
                    .collect(Collectors.toList()), previewContents);
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return null;
    }

    private Boolean checkCategoryMatch(TemplateMarketDTO template, String categoryNameMatch) {
        try {
            return template.getCategories().stream()
                    .anyMatch(category -> categoryNameMatch.equals(category.getName()));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private MarketBaseResponse baseResponseV2TransRecommend(MarketTemplateV2BaseResponse v2BaseResponse, List<TemplateMarketDTO> templateManages, String url) {
        Map<String, Long> useTime = coreOptRecentManage.findTemplateRecentUseTime();
        List<MarketMetaDataVO> categoryVO = getCategoriesV2().stream().filter(node -> !"全部".equalsIgnoreCase(node.getLabel())).collect(Collectors.toList());
        Map<String, String> categoriesMap = categoryVO.stream()
                .collect(Collectors.toMap(MarketMetaDataVO::getSlug, MarketMetaDataVO::getLabel));
        List<TemplateMarketDTO> contents = new ArrayList<>();
        if (v2BaseResponse != null) {
            v2BaseResponse.getItems().stream().forEach(marketTemplateV2ItemResult -> {
                MarketApplicationSpecVO spec = marketTemplateV2ItemResult.getApplication().getSpec();
                MarketApplicationMetaDataVO metadata = marketTemplateV2ItemResult.getApplication().getMetadata();
                if ("Y".equalsIgnoreCase(spec.getSuggest())) {
                    contents.add(new TemplateMarketDTO(metadata.getName(), spec.getDisplayName(), spec.getScreenshots().get(0).getUrl(), spec.getLinks().get(0).getUrl(), categoriesMap.get(spec.getLabel()), spec.getTemplateType(), useTime.get(spec.getReadmeName()), "Y"));
                }
            });
        }
        // 最近使用排序
        Collections.sort(contents);
        Long countDataV = contents.stream().filter(item -> "PANEL".equals(item.getTemplateType())).count();
        Long countDashboard = contents.stream().filter(item -> "SCREEN".equals(item.getTemplateType())).count();
        List<TemplateMarketDTO> templateDataV = templateManages.stream().filter(item -> "PANEL".equals(item.getTemplateType())).collect(Collectors.toList());
        List<TemplateMarketDTO> templateDashboard = templateManages.stream().filter(item -> "SCREEN".equals(item.getTemplateType())).collect(Collectors.toList());
        if (countDataV < 10) {
            Long addItemCount = 10 - countDataV;
            Long addIndex = templateDataV.size() < addItemCount ? templateDataV.size() : addItemCount;
            contents.addAll(templateDataV.subList(0, addIndex.intValue()));
        }

        if (countDashboard < 10) {
            Long addItemCount = 10 - countDashboard;
            Long addIndex = templateDashboard.size() < addItemCount ? templateDashboard.size() : addItemCount;
            contents.addAll(templateDashboard.subList(0, addIndex.intValue()));
        }

        return new MarketBaseResponse(url, categoryVO, contents);
    }

    private MarketBaseResponse baseResponseV2Trans(MarketTemplateV2BaseResponse v2BaseResponse, List<TemplateMarketDTO> contents, String url) {
        Map<String, Long> useTime = coreOptRecentManage.findTemplateRecentUseTime();
        List<MarketMetaDataVO> categoryVO = getCategoriesObject().stream().filter(node -> !"全部".equalsIgnoreCase(node.getLabel())).collect(Collectors.toList());
        Map<String, String> categoriesMap = categoryVO.stream()
                .collect(Collectors.toMap(MarketMetaDataVO::getValue, MarketMetaDataVO::getLabel));
        List<String> activeCategoriesName = new ArrayList<>(Arrays.asList("最近使用", "推荐"));
        contents.stream().forEach(templateMarketDTO -> {
            Long recentUseTime = useTime.get(templateMarketDTO.getId());
            templateMarketDTO.setRecentUseTime(recentUseTime == null ? 0 : recentUseTime);
            activeCategoriesName.addAll(templateMarketDTO.getCategoryNames());
        });
        if (v2BaseResponse != null) {
            v2BaseResponse.getItems().stream().forEach(marketTemplateV2ItemResult -> {
                MarketApplicationSpecVO spec = marketTemplateV2ItemResult.getApplication().getSpec();
                MarketApplicationMetaDataVO metadata = marketTemplateV2ItemResult.getApplication().getMetadata();
                contents.add(new TemplateMarketDTO(metadata.getName(), spec.getDisplayName(), spec.getScreenshots().get(0).getUrl(), spec.getLinks().get(0).getUrl(), categoriesMap.get(spec.getLabel()), spec.getTemplateType(), useTime.get(spec.getReadmeName()), spec.getSuggest()));
                if (categoriesMap.get(spec.getLabel()) != null) {
                    activeCategoriesName.add(categoriesMap.get(spec.getLabel()));
                }
            });
        }
        // 最近使用排序
        Collections.sort(contents);
        return new MarketBaseResponse(url, categoryVO.stream().filter(node -> activeCategoriesName.contains(node.getLabel())).collect(Collectors.toList()), contents);
    }


    public List<String> getCategories() {
        return getCategoriesV2().stream().map(MarketMetaDataVO::getLabel)
                .collect(Collectors.toList());
    }

    public List<MarketMetaDataVO> getCategoriesObject() {
        List<MarketMetaDataVO> result = getCategoriesV2();
        result.add(0, new MarketMetaDataVO("recent", "最近使用", CommonConstants.TEMPLATE_SOURCE.PUBLIC));
        return result;
    }

    public Map<String, String> getCategoriesBaseV2() {
        Map<String, String> categories = getCategoriesV2().stream()
                .collect(Collectors.toMap(MarketMetaDataVO::getSlug, MarketMetaDataVO::getLabel));
        return categories;
    }

    public List<MarketMetaDataVO> getCategoriesV2() {
        List<MarketMetaDataVO> allCategories = new ArrayList<>();
        List<TemplateManageDTO> manageCategories = templateManageMapper.findCategories(null);
        List<MarketMetaDataVO> manageCategoriesTrans = manageCategories.stream()
                .map(templateCategory -> new MarketMetaDataVO(templateCategory.getId(), templateCategory.getName(), CommonConstants.TEMPLATE_SOURCE.MANAGE))
                .collect(Collectors.toList());
        try {
            Map<String, String> templateParams = sysParameterManage.groupVal("template.");
            String resultStr = marketGet(templateParams.get("template.url") + TEMPLATE_META_DATA_URL, null);
            MarketMetaDataBaseResponse metaData = JsonUtil.parseObject(resultStr, MarketMetaDataBaseResponse.class);
            allCategories.addAll(metaData.getLabels());
            allCategories.add(0, new MarketMetaDataVO("suggest", "推荐", CommonConstants.TEMPLATE_SOURCE.PUBLIC));
        } catch (Exception e) {
            LogUtil.error("模板市场分类获取错误", e);
        }

        return mergeAndDistinctByLabel(allCategories, manageCategoriesTrans);

    }

    private List<MarketMetaDataVO> mergeAndDistinctByLabel(List<MarketMetaDataVO> list1, List<MarketMetaDataVO> list2) {
        List<MarketMetaDataVO> mergedList = new ArrayList<>(list1);
        mergedList.addAll(list2);
        Map<String, MarketMetaDataVO> marketMetaDataMap = mergedList.stream()
                .collect(Collectors.toMap(
                        MarketMetaDataVO::getLabel,
                        Function.identity(),
                        (existing, replacement) -> {
                            existing.setSource(CommonConstants.TEMPLATE_SOURCE.PUBLIC);
                            return existing;
                        },
                        LinkedHashMap::new
                ));
        return new ArrayList<>(marketMetaDataMap.values());
    }
}
