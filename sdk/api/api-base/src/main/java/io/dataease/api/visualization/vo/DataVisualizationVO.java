package io.dataease.api.visualization.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.google.gson.Gson;
import io.dataease.api.template.dto.VisualizationTemplateExtendDataDTO;
import io.dataease.extensions.view.dto.ChartViewDTO;
import io.dataease.utils.JsonUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class DataVisualizationVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 父id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pid;

    /**
     * 所属组织id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;

    /**
     * 层级
     */
    private Integer level;

    /**
     * 节点类型  folder or panel 目录或者文件夹
     */
    private String nodeType;

    /**
     * 类型
     */
    private String type;

    /**
     * 样式数据
     */
    private String canvasStyleData;

    /**
     * 组件数据
     */
    private String componentData;

    /**
     * 移动端布局
     */
    private Boolean mobileLayout;
    /**
     * 移动端布局
     */
    private Integer extFlag;

    /**
     * 状态 0-未发布 1-已发布
     */
    private Integer status;

    /**
     * 是否单独打开水印 0-关闭 1-开启
     */
    private Boolean selfWatermarkStatus;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 创建人
     */
    private String createBy;
    /**
     * 创建人姓名
     */
    private String creatorName;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 更新人
     */
    private String updateBy;
    /**
     * 更新人姓名
     */
    private String updateName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 数据来源
     */
    private String source;

    /**
     * 删除标志
     */
    private Boolean deleteFlag;

    /**
     * 删除时间
     */
    private Long deleteTime;

    /**
     * 删除人
     */
    private String deleteBy;

    /**
     * 可视化资源版本
     */
    private Integer version;

    /**
     * 图表基本信息
     */
    private Map<Long, ChartViewDTO> canvasViewInfo = new HashMap<>();

    /**
     * 图表模板数据
     */
    private Map<Long, VisualizationTemplateExtendDataDTO> extendDataInfo = new HashMap<>();


    /**
     * 定时报告自定义过滤数据
     */
    private Map<Long, VisualizationReportFilterVO> reportFilterInfo = new HashMap<>();

    /**
     * 水印信息
     */
    private VisualizationWatermarkVO watermarkInfo;

    /**
     * 权限信息
     */
    private Integer weight;

    /**
     * 应用信息
     */
    private VisualizationExport2AppVO appData;


    public DataVisualizationVO(Long id, String name, String type, Integer version, String canvasStyleData, String componentData,String appDataStr, Map<Long, ChartViewDTO> canvasViewInfo, Map<Long, VisualizationTemplateExtendDataDTO> extendDataInfo) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.canvasStyleData = canvasStyleData;
        this.componentData = componentData;
        this.canvasViewInfo = canvasViewInfo;
        this.extendDataInfo = extendDataInfo;
        if(StringUtils.isNotEmpty(appDataStr)){
            this.appData= JsonUtil.parseObject(appDataStr,VisualizationExport2AppVO.class);
        }
        this.version = version;
    }
}
