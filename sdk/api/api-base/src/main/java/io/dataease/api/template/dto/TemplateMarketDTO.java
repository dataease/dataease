package io.dataease.api.template.dto;

import io.dataease.api.template.vo.MarketCategoryVO;
import io.dataease.api.template.vo.MarketMetasVO;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Arrays;
import java.util.List;

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
    private List<MarketCategoryVO> categories;

    private String mainCategory;
    private MarketMetasVO metas;


    public TemplateMarketDTO(TemplateManageDTO manageDTO) {
        this.id = manageDTO.getId();
        this.title = manageDTO.getName();
        this.mainCategory = manageDTO.getCategoryName();
        this.categories = Arrays.asList(new MarketCategoryVO(manageDTO.getCategoryName()), new MarketCategoryVO("全部"));
        this.metas = new MarketMetasVO(manageDTO.getSnapshot());
        this.templateType = "dataV".equalsIgnoreCase("manageDTO.getTemplateType()") ? "SCREEN" : "PANEL";
        this.thumbnail = manageDTO.getSnapshot();
        this.source = "manage";
        if (manageDTO.getRecentUseTime() != null) {
            this.recentUseTime = manageDTO.getRecentUseTime();
        }
    }

    public TemplateMarketDTO(String id, String title, String themeRepo, String templateUrl, String categoryName, String templateType, Long recentUseTime, String suggest) {
        this.id = id;
        this.title = title;
        this.categories = Arrays.asList(new MarketCategoryVO(categoryName), new MarketCategoryVO("全部"));
        this.metas = new MarketMetasVO(templateUrl);
        this.thumbnail = themeRepo;
        this.templateType = templateType;
        if (recentUseTime != null) {
            this.recentUseTime = recentUseTime;
        }
        if ("Y".equalsIgnoreCase(suggest)) {
            this.suggest = "Y";
        }
    }

    @Override
    public int compareTo(TemplateMarketDTO other) {
        return Long.compare(other.recentUseTime, this.recentUseTime);
    }
}
