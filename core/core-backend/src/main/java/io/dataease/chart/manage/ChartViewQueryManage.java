package io.dataease.chart.manage;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.chart.dao.auto.mapper.CoreChartViewRepository;
import io.dataease.constant.AuthConstant;
import io.dataease.dao.auto.entity.CoreChartView;
import io.dataease.dao.auto.entity.CoreDatasetTableField;
import io.dataease.dao.auto.repo.CoreDatasetTableFieldRepository;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetGroupRepository;
import io.dataease.dataset.manage.PermissionManage;
import io.dataease.engine.constant.ExtFieldConstant;
import io.dataease.engine.func.FunctionConstant;
import io.dataease.engine.utils.Utils;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.api.PluginManageApi;
import io.dataease.extensions.datasource.dto.CalParam;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.datasource.dto.FieldGroupDTO;
import io.dataease.extensions.datasource.model.SQLObj;
import io.dataease.extensions.view.dto.ChartFieldCompareDTO;
import io.dataease.extensions.view.dto.ChartViewDTO;
import io.dataease.extensions.view.dto.ChartViewFieldDTO;
import io.dataease.extensions.view.dto.ColumnPermissionItem;
import io.dataease.extensions.view.dto.FormatterCfgDTO;
import io.dataease.i18n.Lang;
import io.dataease.i18n.Translator;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.JsonUtil;
import io.dataease.utils.ServletUtils;
import io.dataease.visualization.dao.auto.entity.SnapshotCoreChartView;
import io.dataease.visualization.dao.auto.mapper.SnapshotCoreChartViewRepository;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 图表视图只读/快照查询服务：checkLinkChart、listByDQ、save 及字段转换。
 * 不依赖 ChartDataManage，避免与图表计算形成环。
 */
@Component
public class ChartViewQueryManage {

    @Resource
    private SnapshotCoreChartViewRepository snapshotCoreChartViewRepository;
    @Resource
    private CoreChartViewRepository coreChartViewRepository;
    @Resource
    private CoreDatasetGroupRepository coreDatasetGroupRepository;
    @Resource
    private CoreDatasetTableFieldRepository coreDatasetTableFieldRepository;
    @Resource
    private PermissionManage permissionManage;
    @Autowired(required = false)
    private PluginManageApi pluginManage;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public ChartViewDTO save(ChartViewDTO chartViewDTO) throws Exception {
        if (chartViewDTO.getTitle().length() > 100) {
            DEException.throwException(Translator.get("i18n_name_limit_100"));
        }
        Long id = chartViewDTO.getId();
        if (id == null) {
            DEException.throwException(Translator.get("i18n_no_id"));
        }
        SnapshotCoreChartView coreChartView = snapshotCoreChartViewRepository.findById(id).orElse(null);
        SnapshotCoreChartView record = transDTO2Record(chartViewDTO);
        if (org.apache.commons.lang3.ObjectUtils.isEmpty(coreChartView)) {
            snapshotCoreChartViewRepository.deleteById(record.getId());
        }
        snapshotCoreChartViewRepository.saveAndFlush(record);
        return chartViewDTO;
    }

    public void checkLinkChart(ChartViewDTO view) {
        Long resourceId = view.getSceneId();
        Long viewId = view.getId();
        Long tableId = view.getTableId();

        String linkToken = ServletUtils.getHead(AuthConstant.LINK_TOKEN_KEY);
        if (StringUtils.isBlank(linkToken)) {
            return;
        }

        DecodedJWT jwt = JWT.decode(linkToken);
        Long tokenResourceId = jwt.getClaim("resourceId").asLong();
        if (!tokenResourceId.equals(resourceId)) {
            DEException.throwException("超出分享链接权限");
        }

        CoreChartView chartView = coreChartViewRepository.findById(viewId).orElse(null);
        if (chartView == null) {
            DEException.throwException(Translator.get("i18n_chart_delete"));
        }

        if (!chartView.getTableId().equals(tableId)) {
            DEException.throwException("超出分享链接权限");
        }
    }

    public Map<String, List<ChartViewFieldDTO>> listByDQ(Long id, Long chartId, ChartViewDTO chartViewDTO) {
        // 历史实例引用的数据集已删除时，不生成孤立的系统记录数字段。
        if (!coreDatasetGroupRepository.existsById(id)) {
            return Map.of("dimensionList", List.of(), "quotaList", List.of());
        }
        TypeReference<List<CalParam>> typeToken = new TypeReference<>() {
        };
        TypeReference<List<FieldGroupDTO>> groupTokenType = new TypeReference<>() {
        };
        Specification<CoreDatasetTableField> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("datasetGroupId"), id));
            predicates.add(cb.isTrue(root.get("checked")));
            predicates.add(cb.isNull(root.get("chartId")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<CoreDatasetTableField> fields = coreDatasetTableFieldRepository.findAll(spec);
        List<DatasetTableFieldDTO> collect = fields.stream().map(ele -> {
            DatasetTableFieldDTO dto = new DatasetTableFieldDTO();
            BeanUtils.copyBean(dto, ele);
            dto.setParams(JsonUtil.parseList(ele.getParams(), typeToken));
            dto.setGroupList(JsonUtil.parseList(ele.getGroupList(), groupTokenType));
            return dto;
        }).collect(Collectors.toList());
        // 获取图表计算字段
        List<CoreDatasetTableField> chartFields = new ArrayList<>();
        if (chartId != null && chartId > 0) {
            chartFields = coreDatasetTableFieldRepository.findByChartId(chartId);
        }
        chartFields.stream().filter(ele -> ele.getDatasetGroupId() == null
                || Objects.equals(ele.getDatasetGroupId(), id)).forEach(ele -> {
            DatasetTableFieldDTO dto = new DatasetTableFieldDTO();
            BeanUtils.copyBean(dto, ele);
            dto.setParams(JsonUtil.parseList(ele.getParams(), typeToken));
            dto.setGroupList(JsonUtil.parseList(ele.getGroupList(), groupTokenType));
            collect.add(dto);
        });
        // 原字段和当前数据集副本一起过滤，副本才能继承来源字段的权限与脱敏。
        Map<String, ColumnPermissionItem> desensitizationList = new HashMap<>();
        List<DatasetTableFieldDTO> datasetTableFieldDTOS = permissionManage.filterColumnPermissions(collect, desensitizationList, id, null);
        datasetTableFieldDTOS.forEach(ele -> ele.setDesensitized(desensitizationList.containsKey(ele.getDataeaseName())));
        int countFieldIndex = (int) datasetTableFieldDTOS.stream().filter(ele -> ele.getChartId() == null).count();
        datasetTableFieldDTOS.add(countFieldIndex, createCountField(id));
        List<ChartViewFieldDTO> list = transFieldDTO(datasetTableFieldDTOS);

        // 获取list中的聚合函数，将字段的summary设置成空
        SQLObj tableObj = new SQLObj();
        tableObj.setTableAlias("");

        for (ChartViewFieldDTO ele : list) {
            if (Objects.equals(ele.getExtField(), ExtFieldConstant.EXT_CALC)) {
                List<DatasetTableFieldDTO> f = list.stream().map(e -> {
                    DatasetTableFieldDTO dto = new DatasetTableFieldDTO();
                    BeanUtils.copyBean(dto, e);
                    return dto;
                }).collect(Collectors.toList());
                String originField = Utils.calcFieldRegex(ele, tableObj, f, true, null, Utils.mergeParam(Utils.getParams(f), null), pluginManage);
                for (String func : FunctionConstant.AGG_FUNC) {
                    if (Utils.matchFunction(func, originField)) {
                        ele.setSummary("");
                        ele.setAgg(true);
                        break;
                    }
                }
            }
        }

        List<ChartViewFieldDTO> dimensionList = list.stream().filter(ele -> StringUtils.equalsIgnoreCase(ele.getGroupType(), "d")).collect(Collectors.toList());
        List<ChartViewFieldDTO> quotaList = list.stream().filter(ele -> StringUtils.equalsIgnoreCase(ele.getGroupType(), "q")).collect(Collectors.toList());

        Map<String, List<ChartViewFieldDTO>> map = new LinkedHashMap<>();
        map.put("dimensionList", dimensionList);
        map.put("quotaList", quotaList);
        return map;
    }

    public DatasetTableFieldDTO createCountField(Long id) {
        DatasetTableFieldDTO dto = new DatasetTableFieldDTO();
        dto.setId(-1L);
        dto.setDatasetGroupId(id);
        dto.setOriginName("*");
        dto.setName("记录数*");
        dto.setDataeaseName("*");
        dto.setType("INT");
        dto.setChecked(true);
        dto.setColumnIndex(999);
        dto.setDeType(2);
        dto.setExtField(1);
        dto.setGroupType("q");
        return dto;
    }

    public List<ChartViewFieldDTO> transFieldDTO(List<DatasetTableFieldDTO> list) {
        return list.stream().map(ele -> {
            ChartViewFieldDTO dto = new ChartViewFieldDTO();
            if (ele == null) return null;
            BeanUtils.copyBean(dto, ele);
            dto.setDateStyle("y_M_d");
            dto.setDatePattern("date_sub");
            dto.setDateShowFormat("y_M_d");
            dto.setChartType("bar");

            if (dto.getId() == -1L || dto.getDeType() == 0 || dto.getDeType() == 1 || dto.getDeType() == 7) {
                dto.setSummary("count");
            } else {
                dto.setSummary("sum");
            }

            ChartFieldCompareDTO chartFieldCompareDTO = new ChartFieldCompareDTO();
            chartFieldCompareDTO.setType("none");
            dto.setCompareCalc(chartFieldCompareDTO);

            dto.setFormatterCfg(new FormatterCfgDTO().setUnitLanguage(Lang.isChinese() ? "ch" : "en"));

            dto.setSort("none");
            dto.setFilter(Collections.emptyList());
            return dto;
        }).collect(Collectors.toList());
    }

    private SnapshotCoreChartView transDTO2Record(ChartViewDTO dto) throws Exception {
        SnapshotCoreChartView record = new SnapshotCoreChartView();
        BeanUtils.copyBean(record, dto);
        record.setXAxis(objectMapper.writeValueAsString(dto.getXAxis()));
        record.setXAxisExt(objectMapper.writeValueAsString(dto.getXAxisExt()));
        record.setYAxis(objectMapper.writeValueAsString(dto.getYAxis()));
        record.setYAxisExt(objectMapper.writeValueAsString(dto.getYAxisExt()));
        record.setExtStack(objectMapper.writeValueAsString(dto.getExtStack()));
        record.setExtBubble(objectMapper.writeValueAsString(dto.getExtBubble()));
        record.setExtLabel(objectMapper.writeValueAsString(dto.getExtLabel()));
        record.setExtTooltip(objectMapper.writeValueAsString(dto.getExtTooltip()));
        record.setCustomAttr(objectMapper.writeValueAsString(dto.getCustomAttr()));
        if (dto.getCustomAttrMobile() != null) {
            record.setCustomAttrMobile(objectMapper.writeValueAsString(dto.getCustomAttrMobile()));
        }
        record.setCustomStyle(objectMapper.writeValueAsString(dto.getCustomStyle()));
        if (dto.getCustomAttrMobile() != null) {
            record.setCustomStyleMobile(objectMapper.writeValueAsString(dto.getCustomStyleMobile()));
        }
        record.setSenior(objectMapper.writeValueAsString(dto.getSenior()));
        record.setDrillFields(objectMapper.writeValueAsString(dto.getDrillFields()));
        record.setCustomFilter(objectMapper.writeValueAsString(dto.getCustomFilter()));
        record.setViewFields(objectMapper.writeValueAsString(dto.getViewFields()));
        record.setFlowMapStartName(objectMapper.writeValueAsString(dto.getFlowMapStartName()));
        record.setFlowMapEndName(objectMapper.writeValueAsString(dto.getFlowMapEndName()));
        record.setExtColor(objectMapper.writeValueAsString(dto.getExtColor()));
        record.setSortPriority(objectMapper.writeValueAsString(dto.getSortPriority()));
        return record;
    }
}
