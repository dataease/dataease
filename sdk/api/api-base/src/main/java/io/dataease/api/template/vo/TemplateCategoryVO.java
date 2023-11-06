package io.dataease.api.template.vo;

import lombok.Data;

/**
 * Author: wangjiahao
 * Date: 2022/7/18
 * Description:
 */
@Data
public class TemplateCategoryVO {
    private Integer id;

    private String name;

    private String slug;

    private Integer priority;
}
