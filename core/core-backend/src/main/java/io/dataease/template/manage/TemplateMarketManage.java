package io.dataease.template.manage;

import com.fasterxml.jackson.core.type.TypeReference;
import io.dataease.api.template.dto.TemplateManageFileDTO;
import io.dataease.api.template.dto.TemplateMarketDTO;
import io.dataease.api.template.request.TemplateMarketSearchRequest;
import io.dataease.api.template.response.MarketBaseResponse;
import io.dataease.api.template.vo.TemplateCategoryVO;
import io.dataease.exception.DEException;
import io.dataease.system.manage.SysParameterManage;
import io.dataease.utils.HttpClientConfig;
import io.dataease.utils.HttpClientUtil;
import io.dataease.utils.JsonUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Author: wangjiahao
 */
@Service
public class TemplateMarketManage {

    private final static String POSTS_API = "/api/content/posts?page=0&size=2000";
    private final static String CATEGORIES_API = "/api/content/categories";

    @Resource
    private SysParameterManage sysParameterManage;

    /**
     * @param templateUrl template url
     * @Description Get template file from template market
     */
    public TemplateManageFileDTO getTemplateFromMarket(String templateUrl) {
        if (StringUtils.isNotEmpty(templateUrl)) {
            String sufUrl = sysParameterManage.groupVal("template.").get("template.url");
            String templateInfo = HttpClientUtil.get(sufUrl + templateUrl, null);
            return JsonUtil.parseObject(templateInfo,TemplateManageFileDTO.class);
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

    public MarketBaseResponse searchTemplate(TemplateMarketSearchRequest request) {
        try {
            Map<String,String>  templateParams = sysParameterManage.groupVal("template.");
            String result = marketGet(templateParams.get("template.url") + POSTS_API, templateParams.get("template.accessKey"));
            TypeReference<List<TemplateMarketDTO>> market = new TypeReference<>() {
            };
            List<TemplateMarketDTO> postsResult = JsonUtil.parseList(result,market);
            return new MarketBaseResponse(templateParams.get("template.url"), postsResult);
        } catch (Exception e) {
            DEException.throwException(e);
        }
        return null;
    }

    public List<String> getCategories() {
        Map<String,String>  templateParams = sysParameterManage.groupVal("template.");
        String resultStr = marketGet(templateParams.get("template.url") + CATEGORIES_API, templateParams.get("template.accessKey"));
        TypeReference<List<TemplateCategoryVO>> market = new TypeReference<>() {
        };
        List<TemplateCategoryVO> categories = JsonUtil.parseList(resultStr,market);
        if (CollectionUtils.isNotEmpty(categories)) {
            return categories.stream().filter(item -> !"应用系列".equals(item.getName())).sorted(Comparator.comparing(TemplateCategoryVO::getPriority)).map(TemplateCategoryVO::getName).collect(Collectors.toList());
        } else {
            return null;
        }

    }
}
