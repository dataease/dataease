package io.dataease.chart.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 组件图表表
 * </p>
 *
 * @author fit2cloud
 * @since 2024-05-07
 */
@TableName("core_chart_view")
public class CoreChartView implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 场景ID chart_type为private的时候 是仪表板id
     */
    private Long sceneId;

    /**
     * 数据集表ID
     */
    private Long tableId;

    /**
     * 图表类型
     */
    private String type;

    /**
     * 图表渲染方式
     */
    private String render;

    /**
     * 展示结果
     */
    private Integer resultCount;

    /**
     * 展示模式
     */
    private String resultMode;

    /**
     * 横轴field
     */
    private String xAxis;

    /**
     * table-row
     */
    private String xAxisExt;

    /**
     * 纵轴field
     */
    private String yAxis;

    /**
     * 副轴
     */
    private String yAxisExt;

    /**
     * 堆叠项
     */
    private String extStack;

    /**
     * 气泡大小
     */
    private String extBubble;

    /**
     * 动态标签
     */
    private String extLabel;

    /**
     * 动态提示
     */
    private String extTooltip;

    /**
     * 图形属性
     */
    private String customAttr;

    /**
     * 组件样式
     */
    private String customStyle;

    /**
     * 结果过滤
     */
    private String customFilter;

    /**
     * 钻取字段
     */
    private String drillFields;

    /**
     * 高级
     */
    private String senior;

    /**
     * 创建人ID
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 缩略图 
     */
    private String snapshot;

    /**
     * 样式优先级 panel 仪表板 view 图表
     */
    private String stylePriority;

    /**
     * 图表类型 public 公共 历史可复用的图表，private 私有 专属某个仪表板
     */
    private String chartType;

    /**
     * 是否插件
     */
    private Boolean isPlugin;

    /**
     * 数据来源 template 模板数据 dataset 数据集数据
     */
    private String dataFrom;

    /**
     * 图表字段集合
     */
    private String viewFields;

    /**
     * 是否开启刷新
     */
    private Boolean refreshViewEnable;

    /**
     * 刷新时间单位
     */
    private String refreshUnit;

    /**
     * 刷新时间
     */
    private Integer refreshTime;

    /**
     * 是否开启联动
     */
    private Boolean linkageActive;

    /**
     * 是否开启跳转
     */
    private Boolean jumpActive;

    /**
     * 复制来源
     */
    private Long copyFrom;

    /**
     * 复制ID
     */
    private Long copyId;

    /**
     * 区间条形图开启时间纬度开启聚合
     */
    private Boolean aggregate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getSceneId() {
        return sceneId;
    }

    public void setSceneId(Long sceneId) {
        this.sceneId = sceneId;
    }

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRender() {
        return render;
    }

    public void setRender(String render) {
        this.render = render;
    }

    public Integer getResultCount() {
        return resultCount;
    }

    public void setResultCount(Integer resultCount) {
        this.resultCount = resultCount;
    }

    public String getResultMode() {
        return resultMode;
    }

    public void setResultMode(String resultMode) {
        this.resultMode = resultMode;
    }

    public String getxAxis() {
        return xAxis;
    }

    public void setxAxis(String xAxis) {
        this.xAxis = xAxis;
    }

    public String getxAxisExt() {
        return xAxisExt;
    }

    public void setxAxisExt(String xAxisExt) {
        this.xAxisExt = xAxisExt;
    }

    public String getyAxis() {
        return yAxis;
    }

    public void setyAxis(String yAxis) {
        this.yAxis = yAxis;
    }

    public String getyAxisExt() {
        return yAxisExt;
    }

    public void setyAxisExt(String yAxisExt) {
        this.yAxisExt = yAxisExt;
    }

    public String getExtStack() {
        return extStack;
    }

    public void setExtStack(String extStack) {
        this.extStack = extStack;
    }

    public String getExtBubble() {
        return extBubble;
    }

    public void setExtBubble(String extBubble) {
        this.extBubble = extBubble;
    }

    public String getExtLabel() {
        return extLabel;
    }

    public void setExtLabel(String extLabel) {
        this.extLabel = extLabel;
    }

    public String getExtTooltip() {
        return extTooltip;
    }

    public void setExtTooltip(String extTooltip) {
        this.extTooltip = extTooltip;
    }

    public String getCustomAttr() {
        return customAttr;
    }

    public void setCustomAttr(String customAttr) {
        this.customAttr = customAttr;
    }

    public String getCustomStyle() {
        return customStyle;
    }

    public void setCustomStyle(String customStyle) {
        this.customStyle = customStyle;
    }

    public String getCustomFilter() {
        return customFilter;
    }

    public void setCustomFilter(String customFilter) {
        this.customFilter = customFilter;
    }

    public String getDrillFields() {
        return drillFields;
    }

    public void setDrillFields(String drillFields) {
        this.drillFields = drillFields;
    }

    public String getSenior() {
        return senior;
    }

    public void setSenior(String senior) {
        this.senior = senior;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public String getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
    }

    public String getStylePriority() {
        return stylePriority;
    }

    public void setStylePriority(String stylePriority) {
        this.stylePriority = stylePriority;
    }

    public String getChartType() {
        return chartType;
    }

    public void setChartType(String chartType) {
        this.chartType = chartType;
    }

    public Boolean getIsPlugin() {
        return isPlugin;
    }

    public void setIsPlugin(Boolean isPlugin) {
        this.isPlugin = isPlugin;
    }

    public String getDataFrom() {
        return dataFrom;
    }

    public void setDataFrom(String dataFrom) {
        this.dataFrom = dataFrom;
    }

    public String getViewFields() {
        return viewFields;
    }

    public void setViewFields(String viewFields) {
        this.viewFields = viewFields;
    }

    public Boolean getRefreshViewEnable() {
        return refreshViewEnable;
    }

    public void setRefreshViewEnable(Boolean refreshViewEnable) {
        this.refreshViewEnable = refreshViewEnable;
    }

    public String getRefreshUnit() {
        return refreshUnit;
    }

    public void setRefreshUnit(String refreshUnit) {
        this.refreshUnit = refreshUnit;
    }

    public Integer getRefreshTime() {
        return refreshTime;
    }

    public void setRefreshTime(Integer refreshTime) {
        this.refreshTime = refreshTime;
    }

    public Boolean getLinkageActive() {
        return linkageActive;
    }

    public void setLinkageActive(Boolean linkageActive) {
        this.linkageActive = linkageActive;
    }

    public Boolean getJumpActive() {
        return jumpActive;
    }

    public void setJumpActive(Boolean jumpActive) {
        this.jumpActive = jumpActive;
    }

    public Long getCopyFrom() {
        return copyFrom;
    }

    public void setCopyFrom(Long copyFrom) {
        this.copyFrom = copyFrom;
    }

    public Long getCopyId() {
        return copyId;
    }

    public void setCopyId(Long copyId) {
        this.copyId = copyId;
    }

    public Boolean getAggregate() {
        return aggregate;
    }

    public void setAggregate(Boolean aggregate) {
        this.aggregate = aggregate;
    }

    @Override
    public String toString() {
        return "CoreChartView{" +
        "id = " + id +
        ", title = " + title +
        ", sceneId = " + sceneId +
        ", tableId = " + tableId +
        ", type = " + type +
        ", render = " + render +
        ", resultCount = " + resultCount +
        ", resultMode = " + resultMode +
        ", xAxis = " + xAxis +
        ", xAxisExt = " + xAxisExt +
        ", yAxis = " + yAxis +
        ", yAxisExt = " + yAxisExt +
        ", extStack = " + extStack +
        ", extBubble = " + extBubble +
        ", extLabel = " + extLabel +
        ", extTooltip = " + extTooltip +
        ", customAttr = " + customAttr +
        ", customStyle = " + customStyle +
        ", customFilter = " + customFilter +
        ", drillFields = " + drillFields +
        ", senior = " + senior +
        ", createBy = " + createBy +
        ", createTime = " + createTime +
        ", updateTime = " + updateTime +
        ", snapshot = " + snapshot +
        ", stylePriority = " + stylePriority +
        ", chartType = " + chartType +
        ", isPlugin = " + isPlugin +
        ", dataFrom = " + dataFrom +
        ", viewFields = " + viewFields +
        ", refreshViewEnable = " + refreshViewEnable +
        ", refreshUnit = " + refreshUnit +
        ", refreshTime = " + refreshTime +
        ", linkageActive = " + linkageActive +
        ", jumpActive = " + jumpActive +
        ", copyFrom = " + copyFrom +
        ", copyId = " + copyId +
        ", aggregate = " + aggregate +
        "}";
    }
}
