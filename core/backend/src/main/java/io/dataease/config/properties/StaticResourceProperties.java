package io.dataease.config.properties;

import lombok.Data;

/**
 * Author: wangjiahao
 * Date: 2022/4/24
 * Description:
 */
@Data
public class StaticResourceProperties {

    /**
     * Upload prefix.
     */
    private String uploadUrlPrefix = "static-resource";

}
