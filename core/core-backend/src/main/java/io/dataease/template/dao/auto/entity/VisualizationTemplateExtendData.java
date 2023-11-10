package io.dataease.template.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author fit2cloud
 * @since 2023-11-10
 */
@TableName("visualization_template_extend_data")
public class VisualizationTemplateExtendData implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long dvId;

    private Long viewId;

    private String viewDetails;

    private String copyFrom;

    private String copyId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDvId() {
        return dvId;
    }

    public void setDvId(Long dvId) {
        this.dvId = dvId;
    }

    public Long getViewId() {
        return viewId;
    }

    public void setViewId(Long viewId) {
        this.viewId = viewId;
    }

    public String getViewDetails() {
        return viewDetails;
    }

    public void setViewDetails(String viewDetails) {
        this.viewDetails = viewDetails;
    }

    public String getCopyFrom() {
        return copyFrom;
    }

    public void setCopyFrom(String copyFrom) {
        this.copyFrom = copyFrom;
    }

    public String getCopyId() {
        return copyId;
    }

    public void setCopyId(String copyId) {
        this.copyId = copyId;
    }

    @Override
    public String toString() {
        return "VisualizationTemplateExtendData{" +
        "id = " + id +
        ", dvId = " + dvId +
        ", viewId = " + viewId +
        ", viewDetails = " + viewDetails +
        ", copyFrom = " + copyFrom +
        ", copyId = " + copyId +
        "}";
    }
}
