package io.dataease.controller.request.resource;

import lombok.Data;

import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2022/4/28
 * Description:
 */
@Data
public class StaticResourceRequest {

    private List<String> resourcePathList;

}
