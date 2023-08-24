package io.dataease.dto;

import java.util.List;

/**
 * Author: wangjiahao
 * Date: 8/10/21
 * Description:
 */
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
