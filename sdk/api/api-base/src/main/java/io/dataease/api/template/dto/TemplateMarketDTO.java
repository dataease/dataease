package io.dataease.api.template.dto;

import io.dataease.api.template.vo.MarketCategoryVO;
import io.dataease.api.template.vo.MarketMetasVO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class TemplateMarketDTO implements Comparable<TemplateMarketDTO> {
    private String id;
    private String title;
    private String status;
    private String slug;
    private String editorType;
    private String summary;
    private String thumbnail;

    private Boolean showFlag = true;

    private String suggest = "N";

    private Long recentUseTime = 0L;

    private String templateType;

    // 模板来源 market = 模板市场；manage=模板管理
    private String source = "market";

    // 模板分类 app = 应用模板；manage=样式模板
    private String classify = "template";
    private List<MarketCategoryVO> categories;

    private List<String> categoryNames;

    private String mainCategory;
    private MarketMetasVO metas;


    public TemplateMarketDTO(TemplateManageDTO manageDTO) {
        this.id = manageDTO.getId();
        this.title = manageDTO.getName();
        this.mainCategory = manageDTO.getCategoryName();
        this.categories = manageDTO.getCategoryNames().stream().map(category->new MarketCategoryVO(category)).collect(Collectors.toList());
        this.categoryNames = manageDTO.getCategoryNames();
        this.metas = new MarketMetasVO(manageDTO.getSnapshot());
        this.templateType = "dataV".equalsIgnoreCase(manageDTO.getDvType()) ? "SCREEN" : "PANEL";
        this.thumbnail = manageDTO.getSnapshot();
        this.source = "manage";
        this.classify = manageDTO.getNodeType();
        if (manageDTO.getRecentUseTime() != null) {
            this.recentUseTime = manageDTO.getRecentUseTime();
            this.categories.add(new MarketCategoryVO("最近使用"));
            this.categoryNames.add("最近使用");
        }
    }

    public TemplateMarketDTO(String id, String title, String themeRepo, String templateUrl, String categoryName, String templateType, Long recentUseTime, String suggest) {
        this.id = id;
        this.title = title;
        this.metas = new MarketMetasVO(templateUrl);
        this.thumbnail = themeRepo;
        this.templateType = templateType;
        this.categories = new ArrayList<>(Arrays.asList(new MarketCategoryVO(categoryName))) ;
        this.categoryNames = new ArrayList<>(Arrays.asList(categoryName)) ;
        if (recentUseTime != null) {
            this.recentUseTime = recentUseTime;
            this.categories.add(new MarketCategoryVO("最近使用"));
            this.categoryNames.add("最近使用");
        }
        if ("Y".equalsIgnoreCase(suggest)) {
            this.suggest = "Y";
            this.categories.add(new MarketCategoryVO("推荐"));
            this.categoryNames.add("推荐");
        }
    }

    @Override
    public int compareTo(TemplateMarketDTO other) {
        return Long.compare(other.recentUseTime, this.recentUseTime);
    }
}
