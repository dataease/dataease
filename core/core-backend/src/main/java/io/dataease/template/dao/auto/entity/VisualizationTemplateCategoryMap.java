package io.dataease.template.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 模板表
 * </p>
 *
 * @author fit2cloud
 * @since 2023-12-04
 */
@TableName("visualization_template_category_map")
public class VisualizationTemplateCategoryMap implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private String id;

    /**
     * 名称
     */
    private String categoryId;

    /**
     * 父级id
     */
    private String templateId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    @Override
    public String toString() {
        return "VisualizationTemplateCategoryMap{" +
        "id = " + id +
        ", categoryId = " + categoryId +
        ", templateId = " + templateId +
        "}";
    }
}
