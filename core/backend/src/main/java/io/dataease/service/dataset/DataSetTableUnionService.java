package io.dataease.service.dataset;

import io.dataease.ext.ExtDatasetTableUnionMapper;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.dto.dataset.DataSetTableUnionDTO;
import io.dataease.i18n.Translator;
import io.dataease.plugins.common.base.domain.DatasetTableUnion;
import io.dataease.plugins.common.base.domain.DatasetTableUnionExample;
import io.dataease.plugins.common.base.mapper.DatasetTableUnionMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @Author gin
 * @Date 2021/5/6 6:03 下午
 */
@Service
@Transactional
public class DataSetTableUnionService {
    @Resource
    private DatasetTableUnionMapper datasetTableUnionMapper;
    @Resource
    private ExtDatasetTableUnionMapper extDatasetTableUnionMapper;

    public DatasetTableUnion save(DatasetTableUnion datasetTableUnion) {
        checkUnion(datasetTableUnion);
        if (StringUtils.isEmpty(datasetTableUnion.getId())) {
            datasetTableUnion.setId(UUID.randomUUID().toString());
            datasetTableUnion.setCreateBy(AuthUtils.getUser().getUsername());
            datasetTableUnion.setCreateTime(System.currentTimeMillis());
            datasetTableUnionMapper.insert(datasetTableUnion);
        } else {
            datasetTableUnionMapper.updateByPrimaryKeySelective(datasetTableUnion);
        }
        return datasetTableUnion;
    }

    public void delete(String id) {
        datasetTableUnionMapper.deleteByPrimaryKey(id);
    }

    public List<DataSetTableUnionDTO> listByTableId(String tableId) {
        List<DataSetTableUnionDTO> sourceList = extDatasetTableUnionMapper.selectBySourceTableId(tableId);
        List<DataSetTableUnionDTO> targetList = extDatasetTableUnionMapper.selectByTargetTableId(tableId);
        sourceList.addAll(targetList.stream().map(ele -> {
            DataSetTableUnionDTO dto = new DataSetTableUnionDTO();
            dto.setId(ele.getId());

            dto.setSourceTableId(ele.getTargetTableId());
            dto.setSourceTableFieldId(ele.getTargetTableFieldId());
            dto.setSourceTableName(ele.getTargetTableName());
            dto.setSourceTableFieldName(ele.getTargetTableFieldName());

            dto.setTargetTableId(ele.getSourceTableId());
            dto.setTargetTableFieldId(ele.getSourceTableFieldId());
            dto.setTargetTableName(ele.getSourceTableName());
            dto.setTargetTableFieldName(ele.getSourceTableFieldName());

            dto.setSourceUnionRelation(ele.getTargetUnionRelation());
            dto.setTargetUnionRelation(ele.getSourceUnionRelation());

            dto.setCreateBy(ele.getCreateBy());
            dto.setCreateTime(ele.getCreateTime());
            return dto;
        }).collect(Collectors.toList()));

        sourceList.sort(Comparator.comparing(DatasetTableUnion::getCreateTime));
        return sourceList;
    }

    public void deleteUnionByTableId(String tableId) {
        DatasetTableUnionExample datasetTableUnionExample = new DatasetTableUnionExample();
        DatasetTableUnionExample.Criteria criteriaSource = datasetTableUnionExample.createCriteria().andSourceTableIdEqualTo(tableId);
        DatasetTableUnionExample.Criteria criteriaTarget = datasetTableUnionExample.createCriteria().andTargetTableIdEqualTo(tableId);
        datasetTableUnionExample.or(criteriaTarget);
        datasetTableUnionMapper.deleteByExample(datasetTableUnionExample);
    }

    private void checkUnion(DatasetTableUnion datasetTableUnion) {
        // check 关联关系是否存在
        DatasetTableUnionExample datasetTableUnionExample = new DatasetTableUnionExample();
        DatasetTableUnionExample.Criteria criteria = datasetTableUnionExample.createCriteria();
        if (StringUtils.isNotEmpty(datasetTableUnion.getId())) {
            criteria.andIdNotEqualTo(datasetTableUnion.getId());
        }
        criteria.andSourceTableIdEqualTo(datasetTableUnion.getSourceTableId());
        criteria.andSourceTableFieldIdEqualTo(datasetTableUnion.getSourceTableFieldId());
        criteria.andTargetTableIdEqualTo(datasetTableUnion.getTargetTableId());
        criteria.andTargetTableFieldIdEqualTo(datasetTableUnion.getTargetTableFieldId());
        List<DatasetTableUnion> sourceResult = datasetTableUnionMapper.selectByExample(datasetTableUnionExample);
        datasetTableUnionExample.clear();
        criteria = datasetTableUnionExample.createCriteria();
        if (StringUtils.isNotEmpty(datasetTableUnion.getId())) {
            criteria.andIdNotEqualTo(datasetTableUnion.getId());
        }
        criteria.andSourceTableIdEqualTo(datasetTableUnion.getTargetTableId());
        criteria.andSourceTableFieldIdEqualTo(datasetTableUnion.getTargetTableFieldId());
        criteria.andTargetTableIdEqualTo(datasetTableUnion.getSourceTableId());
        criteria.andTargetTableFieldIdEqualTo(datasetTableUnion.getSourceTableFieldId());
        List<DatasetTableUnion> targetResult = datasetTableUnionMapper.selectByExample(datasetTableUnionExample);
        if (CollectionUtils.isNotEmpty(sourceResult) || CollectionUtils.isNotEmpty(targetResult)) {
            throw new RuntimeException(Translator.get("i18n_union_already_exists"));
        }
        // check 同一字段是否在两个关联表中重复出现
        List<DataSetTableUnionDTO> sourceResult1 = extDatasetTableUnionMapper.selectUsedFieldBySource(datasetTableUnion);
        List<DataSetTableUnionDTO> targetResult1 = extDatasetTableUnionMapper.selectUsedFieldByTarget(datasetTableUnion);
        if (CollectionUtils.isNotEmpty(sourceResult1) || CollectionUtils.isNotEmpty(targetResult1)) {
            throw new RuntimeException(Translator.get("i18n_union_field_exists"));
        }
    }
}
