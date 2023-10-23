package io.dataease.api.visualization.dto;

import java.util.List;

public class LinkageInfoDTO {

    private String sourceInfo;

    private List<String> targetInfoList;

    public String getSourceInfo() {
        return sourceInfo;
    }

    public void setSourceInfo(String sourceInfo) {
        this.sourceInfo = sourceInfo;
    }

    public List<String> getTargetInfoList() {
        return targetInfoList;
    }

    public void setTargetInfoList(List<String> targetInfoList) {
        this.targetInfoList = targetInfoList;
    }
}
