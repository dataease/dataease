package io.dataease.service.chart;

import io.dataease.commons.exception.DEException;
import io.dataease.commons.utils.TableUtils;
import io.dataease.i18n.Translator;
import io.dataease.plugins.common.base.domain.ChartViewField;
import io.dataease.plugins.common.base.domain.ChartViewFieldExample;
import io.dataease.plugins.common.base.mapper.ChartViewFieldMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

/**
 * Author: Gin
 */
@Service
public class ChartViewFieldService {
    @Resource
    private ChartViewFieldMapper chartViewFieldMapper;

    public ChartViewField save(ChartViewField chartViewField) {
        checkFieldName(chartViewField);
        if (StringUtils.isEmpty(chartViewField.getId())) {
            chartViewField.setId(UUID.randomUUID().toString());
            // 若dataeasename为空，则用MD5(id)作为dataeasename
            if (StringUtils.isEmpty(chartViewField.getDataeaseName())) {
                chartViewField.setDataeaseName(TableUtils.columnName(chartViewField.getId()));
            }
            if (ObjectUtils.isEmpty(chartViewField.getLastSyncTime())) {
                chartViewField.setLastSyncTime(System.currentTimeMillis());
            }
            chartViewFieldMapper.insert(chartViewField);
        } else {
            chartViewFieldMapper.updateByPrimaryKeySelective(chartViewField);
        }
        return chartViewField;
    }

    public List<ChartViewField> list(ChartViewField chartViewField) {
        ChartViewFieldExample chartViewFieldExample = new ChartViewFieldExample();
        ChartViewFieldExample.Criteria criteria = chartViewFieldExample.createCriteria();
        if (StringUtils.isNotEmpty(chartViewField.getChartId())) {
            criteria.andChartIdEqualTo(chartViewField.getChartId());
        }
        if (StringUtils.isNotEmpty(chartViewField.getGroupType())) {
            criteria.andGroupTypeEqualTo(chartViewField.getGroupType());
        }
        return chartViewFieldMapper.selectByExampleWithBLOBs(chartViewFieldExample);
    }

    public void delete(String id) {
        chartViewFieldMapper.deleteByPrimaryKey(id);
    }

    public void deleteByChartId(String chartId) {
        ChartViewFieldExample chartViewFieldExample = new ChartViewFieldExample();
        chartViewFieldExample.createCriteria().andChartIdEqualTo(chartId);
        chartViewFieldMapper.deleteByExample(chartViewFieldExample);
    }

    public void deleteByChartIds(List<String> chartIds) {
        ChartViewFieldExample chartViewFieldExample = new ChartViewFieldExample();
        chartViewFieldExample.createCriteria().andChartIdIn(chartIds);
        chartViewFieldMapper.deleteByExample(chartViewFieldExample);
    }

    public void checkFieldName(ChartViewField chartViewField) {
        if (StringUtils.isNotEmpty(chartViewField.getName()) && StringUtils.isNotEmpty(chartViewField.getChartId())) {
            ChartViewFieldExample chartViewFieldExample = new ChartViewFieldExample();
            ChartViewFieldExample.Criteria criteria = chartViewFieldExample.createCriteria();
            criteria.andNameEqualTo(chartViewField.getName()).andChartIdEqualTo(chartViewField.getChartId());
            if (StringUtils.isNotEmpty(chartViewField.getId())) {
                criteria.andIdNotEqualTo(chartViewField.getId());
            }
            List<ChartViewField> datasetTableFields = chartViewFieldMapper.selectByExample(chartViewFieldExample);
            if (CollectionUtils.isNotEmpty(datasetTableFields)) {
                DEException.throwException(Translator.get("i18n_field_name_repeat"));
            }
        }
    }

    public void copyField(String sourceChartId, String targetChartId) {
        ChartViewFieldExample chartViewFieldExample = new ChartViewFieldExample();
        chartViewFieldExample.createCriteria().andChartIdEqualTo(sourceChartId);
        List<ChartViewField> chartViewFields = chartViewFieldMapper.selectByExampleWithBLOBs(chartViewFieldExample);
        if (CollectionUtils.isNotEmpty(chartViewFields)) {
            for (ChartViewField field : chartViewFields) {
                field.setId(null);
                field.setChartId(targetChartId);
                save(field);
            }
        }
    }
}
