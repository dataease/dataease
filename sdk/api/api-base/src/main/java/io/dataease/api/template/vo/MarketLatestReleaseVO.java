package io.dataease.api.template.vo;

import lombok.Data;

import java.util.List;

@Data
public class MarketLatestReleaseVO {

    private MarketReleaseVO release;

    private List<MarketReleaseAssetVO> assets;

}
