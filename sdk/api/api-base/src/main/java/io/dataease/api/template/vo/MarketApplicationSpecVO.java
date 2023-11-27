package io.dataease.api.template.vo;

import lombok.Data;

import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2022/7/15
 * Description:
 */
@Data
public class MarketApplicationSpecVO {

    private String displayName;

    private String type;

    private String deVersion;

    private String templateType;

    private String label;

    private String readmeName;

    // 是否推荐
    private String suggest;

    private List<MarketApplicationSpecScreenshotBaseVO> screenshots;

    private List<MarketApplicationSpecLinkVO> links;
}
