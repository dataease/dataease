package io.dataease.dataset.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import io.dataease.api.chart.dto.ChartFieldCustomFilterDTO;
import io.dataease.api.chart.dto.ColumnPermissionItem;
import io.dataease.api.chart.dto.DeSortField;
import io.dataease.api.dataset.dto.DeSortDTO;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.api.dataset.union.UnionDTO;
import io.dataease.api.permissions.dataset.dto.DataSetRowPermissionsTreeDTO;
import io.dataease.dataset.dao.auto.entity.CoreDatasetGroup;
import io.dataease.dataset.dao.auto.entity.CoreDatasetTableField;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetGroupMapper;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetTableFieldMapper;
import io.dataease.datasource.provider.CalciteProvider;
import io.dataease.dto.dataset.DatasetTableFieldDTO;
import io.dataease.exception.DEException;
import io.dataease.utils.*;
import io.dataease.dataset.utils.TableUtils;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author Junjun
 */
@Component
@Transactional
public class DatasetTableFieldManage {
    @Resource
    private CoreDatasetTableFieldMapper coreDatasetTableFieldMapper;
    @Resource
    private PermissionManage permissionManage;
    @Resource
    private CoreDatasetGroupMapper coreDatasetGroupMapper;
    @Resource
    private CalciteProvider calciteProvider;

    public void save(CoreDatasetTableField coreDatasetTableField) {
        if (ObjectUtils.isEmpty(coreDatasetTableField.getId())) {
            coreDatasetTableField.setId(IDUtils.snowID());
            coreDatasetTableFieldMapper.insert(coreDatasetTableField);
        } else {
            coreDatasetTableFieldMapper.updateById(coreDatasetTableField);
        }
    }

    public DatasetTableFieldDTO chartFieldSave(DatasetTableFieldDTO datasetTableFieldDTO) {
        CoreDatasetTableField coreDatasetTableField = coreDatasetTableFieldMapper.selectById(datasetTableFieldDTO.getId());
        QueryWrapper<CoreDatasetTableField> wrapper = new QueryWrapper<>();
        wrapper.eq("name", datasetTableFieldDTO.getName());
        wrapper.eq("chart_id", datasetTableFieldDTO.getChartId());
        if (ObjectUtils.isNotEmpty(coreDatasetTableField)) {
            wrapper.ne("id", datasetTableFieldDTO.getId());
        }
        List<CoreDatasetTableField> fields = coreDatasetTableFieldMapper.selectList(wrapper);
        if (ObjectUtils.isNotEmpty(fields)) {
            DEException.throwException("name duplicated.");
        }
        datasetTableFieldDTO.setDatasetGroupId(null);
        return save(datasetTableFieldDTO);
    }

    /**
     * 数据集保存时使用
     *
     * @param datasetTableFieldDTO
     * @return
     */
    public DatasetTableFieldDTO save(DatasetTableFieldDTO datasetTableFieldDTO) {
        CoreDatasetTableField coreDatasetTableField = coreDatasetTableFieldMapper.selectById(datasetTableFieldDTO.getId());
        CoreDatasetTableField record = new CoreDatasetTableField();
        BeanUtils.copyBean(record, datasetTableFieldDTO);
        if (ObjectUtils.isEmpty(record.getDataeaseName())) {
            String n = TableUtils.fieldNameShort(record.getId() + "");
            record.setFieldShortName(n);
            record.setDataeaseName(n);
        }
        if (ObjectUtils.isEmpty(coreDatasetTableField)) {
            coreDatasetTableFieldMapper.insert(record);
        } else {
            coreDatasetTableFieldMapper.updateById(record);
        }
        return datasetTableFieldDTO;
    }

    public DatasetTableFieldDTO saveField(DatasetTableFieldDTO datasetTableFieldDTO) {
        CoreDatasetTableField record = new CoreDatasetTableField();
        if (ObjectUtils.isEmpty(datasetTableFieldDTO.getId())) {
            datasetTableFieldDTO.setId(IDUtils.snowID());
            BeanUtils.copyBean(record, datasetTableFieldDTO);
            coreDatasetTableFieldMapper.insert(record);
        } else {
            BeanUtils.copyBean(record, datasetTableFieldDTO);
            coreDatasetTableFieldMapper.updateById(record);
        }
        return datasetTableFieldDTO;
    }

    public void deleteById(Long id) {
        coreDatasetTableFieldMapper.deleteById(id);
    }

    public void deleteByDatasetTableUpdate(Long datasetTableId, List<Long> fieldIds) {
        if (!CollectionUtils.isEmpty(fieldIds)) {
            QueryWrapper<CoreDatasetTableField> wrapper = new QueryWrapper<>();
            wrapper.eq("dataset_table_id", datasetTableId);
            wrapper.notIn("id", fieldIds);
            coreDatasetTableFieldMapper.delete(wrapper);
        }
    }

    public void deleteByDatasetGroupUpdate(Long datasetGroupId, List<Long> fieldIds) {
        if (!CollectionUtils.isEmpty(fieldIds)) {
            QueryWrapper<CoreDatasetTableField> wrapper = new QueryWrapper<>();
            wrapper.eq("dataset_group_id", datasetGroupId);
            wrapper.notIn("id", fieldIds);
            coreDatasetTableFieldMapper.delete(wrapper);
        }
    }

    public void deleteByDatasetGroupDelete(Long datasetGroupId) {
        QueryWrapper<CoreDatasetTableField> wrapper = new QueryWrapper<>();
        wrapper.eq("dataset_group_id", datasetGroupId);
        coreDatasetTableFieldMapper.delete(wrapper);
    }

    public void deleteByChartId(Long chartId) {
        QueryWrapper<CoreDatasetTableField> wrapper = new QueryWrapper<>();
        wrapper.eq("chart_id", chartId);
        coreDatasetTableFieldMapper.delete(wrapper);
    }

    public List<DatasetTableFieldDTO> selectByDatasetTableId(Long id) {
        QueryWrapper<CoreDatasetTableField> wrapper = new QueryWrapper<>();
        wrapper.eq("dataset_table_id", id);
        return transDTO(coreDatasetTableFieldMapper.selectList(wrapper));
    }

    public List<DatasetTableFieldDTO> selectByDatasetGroupId(Long id) {
        QueryWrapper<CoreDatasetTableField> wrapper = new QueryWrapper<>();
        wrapper.eq("dataset_group_id", id);
        return transDTO(coreDatasetTableFieldMapper.selectList(wrapper));
    }

    public List<DatasetTableFieldDTO> selectByFieldIds(List<Long> ids) {
        QueryWrapper<CoreDatasetTableField> wrapper = new QueryWrapper<>();
        wrapper.in("id", ids);
        return transDTO(coreDatasetTableFieldMapper.selectList(wrapper));
    }

    public DatasetTableFieldDTO selectById(Long id) {
        CoreDatasetTableField coreDatasetTableField = coreDatasetTableFieldMapper.selectById(id);
        if (coreDatasetTableField == null) return null;
        DatasetTableFieldDTO dto = new DatasetTableFieldDTO();
        BeanUtils.copyBean(dto, coreDatasetTableField);
        return dto;
    }

    /**
     * 返回维度、指标列表
     *
     * @return
     */
    public Map<String, List<DatasetTableFieldDTO>> listByDQ(Long id) {
        QueryWrapper<CoreDatasetTableField> wrapper = new QueryWrapper<>();
        wrapper.eq("dataset_group_id", id);
        List<DatasetTableFieldDTO> list = transDTO(coreDatasetTableFieldMapper.selectList(wrapper));
        List<DatasetTableFieldDTO> dimensionList = list.stream().filter(ele -> StringUtils.equalsIgnoreCase(ele.getGroupType(), "d")).collect(Collectors.toList());
        List<DatasetTableFieldDTO> quotaList = list.stream().filter(ele -> StringUtils.equalsIgnoreCase(ele.getGroupType(), "q")).collect(Collectors.toList());
        Map<String, List<DatasetTableFieldDTO>> map = new LinkedHashMap<>();
        map.put("dimensionList", dimensionList);
        map.put("quotaList", quotaList);
        return map;
    }

    public List<DatasetTableFieldDTO> transDTO(List<CoreDatasetTableField> list) {
        return list.stream().map(ele -> {
            DatasetTableFieldDTO dto = new DatasetTableFieldDTO();
            if (ele == null) return null;
            BeanUtils.copyBean(dto, ele);
            return dto;
        }).collect(Collectors.toList());
    }


    public List<Object> fieldValues(Long fieldId, Long userId, Boolean userPermissions, Boolean rowAndColumnMgm) throws Exception {
        List<Long> fieldIds = new ArrayList<>();
        fieldIds.add(fieldId);
        return fieldValues(fieldIds, null, userId, userPermissions, false, rowAndColumnMgm);
    }


    public List<Object> fieldValues(Long fieldId, DeSortDTO sortDTO, Long userId, Boolean userPermissions, Boolean rowAndColumnMgm) throws Exception {
        List<Long> fieldIds = new ArrayList<>();
        fieldIds.add(fieldId);
        return fieldValues(fieldIds, sortDTO, userId, userPermissions, false, rowAndColumnMgm);
    }

    public List<Object> fieldValues(List<Long> fieldIds, DeSortDTO sortDTO, Long userId, Boolean userPermissions, Boolean needMapping, Boolean rowAndColumnMgm) throws Exception {
        CoreDatasetTableField field = coreDatasetTableFieldMapper.selectById(fieldIds.get(0));
        if (field == null) return null;
        List<DatasetTableFieldDTO> fields = selectByDatasetGroupId(field.getDatasetGroupId());
        List<DeSortField> deSortFields = buildSorts(fields, sortDTO);
        Boolean needSort = !CollectionUtils.isEmpty(deSortFields);
        final List<Long> allTableFieldIds = fields.stream().map(DatasetTableFieldDTO::getId).collect(Collectors.toList());
        boolean multi = fieldIds.stream().anyMatch(item -> !allTableFieldIds.contains(item));
        if (multi && needMapping) {
            DEException.throwException("Cross multiple dataset is not supported");
        }
        List<DatasetTableFieldDTO> permissionFields = fields;
        List<ChartFieldCustomFilterDTO> customFilter = new ArrayList<>();
        List<DataSetRowPermissionsTreeDTO> rowPermissionsTree = new ArrayList<>();
        if (userPermissions) {
            //列权限
            Map<String , ColumnPermissionItem> desensitizationList = new HashMap<>();
            fields = permissionManage.filterColumnPermissions(fields, desensitizationList, field.getDatasetGroupId(), userId);
            Map<Long, DatasetTableFieldDTO> fieldMap = fields.stream().collect(Collectors.toMap(DatasetTableFieldDTO::getId, node -> node));
            permissionFields = fieldIds.stream().map(fieldMap::get).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(permissionFields) || permissionFields.get(0) == null) {
                return new ArrayList<>();
            }
            if (!CollectionUtils.isEmpty(desensitizationList.keySet()) && desensitizationList.keySet().contains(field.getDataeaseName())) {
                List<Object> results = new ArrayList<>();
                results.add(ColumnPermissionItem.CompleteDesensitization);
                return results;
            }
            //行权限
            rowPermissionsTree = permissionManage.getRowPermissionsTree(field.getDatasetGroupId(), userId);
        }
        if (rowAndColumnMgm) {
            Map<Long, DatasetTableFieldDTO> fieldMap = fields.stream().collect(Collectors.toMap(DatasetTableFieldDTO::getId, node -> node));
            permissionFields = fieldIds.stream().map(fieldMap::get).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(permissionFields)) {
                return new ArrayList<>();
            }
        }
        CoreDatasetGroup coreDatasetGroup = coreDatasetGroupMapper.selectById(field.getDatasetGroupId());
        DatasetGroupInfoDTO dto = new DatasetGroupInfoDTO();
        BeanUtils.copyBean(dto, coreDatasetGroup);
        dto.setUnionSql(null);
        List<UnionDTO> unionDTOList = JsonUtil.parseList(coreDatasetGroup.getInfo(), new TypeReference<>() {});
        dto.setUnion(unionDTOList);

        // 获取field
        List<DatasetTableFieldDTO> allFields = permissionFields.stream().map(ele -> {
            DatasetTableFieldDTO datasetTableFieldDTO = new DatasetTableFieldDTO();
            BeanUtils.copyBean(datasetTableFieldDTO, ele);
            datasetTableFieldDTO.setFieldShortName(ele.getDataeaseName());
            return datasetTableFieldDTO;
        }).collect(Collectors.toList());
        dto.setAllFields(allFields);

        //TODO

        List<String[]> rows = new ArrayList<>();
        List<Object> results = rows.stream().map(row -> row[0]).distinct().collect(Collectors.toList());
        results.add("A");
        results.add("B");
        results.add("C");
        return results;
    }

//    private List<BaseTreeNode> buildTreeNode(String[] row, Set<String> pkSet) {
//        List<BaseTreeNode> nodes = new ArrayList<>();
//        List<String> parentPkList = new ArrayList<>();
//        for (int i = 0; i < row.length; i++) {
//            String text = row[i];
//
//            parentPkList.add(text);
//            String val = parentPkList.stream().collect(Collectors.joining(TreeUtils.SEPARATOR));
//            String parentVal = i == 0 ? TreeUtils.DEFAULT_ROOT : row[i - 1];
//            Long pk = parentPkList.stream().collect(Collectors.joining(TreeUtils.SEPARATOR));
//            if (pkSet.contains(pk)) continue;
//            pkSet.add(pk);
//            BaseTreeNode node = new BaseTreeNode(val, parentVal, text, pk + TreeUtils.SEPARATOR + i);
//            nodes.add(node);
//        }
//        return nodes;
//
//    }

    public List<DeSortField> buildSorts(List<DatasetTableFieldDTO> allFields, DeSortDTO sortDTO) {
        if (ObjectUtils.isEmpty(sortDTO) || sortDTO.getId() != null || StringUtils.isBlank(sortDTO.getSort()))
            return null;
        return allFields.stream().filter(field -> sortDTO.getId().equals(field.getId())).map(field -> {
            DeSortField deSortField = BeanUtils.copyBean(new DeSortField(), field);
            deSortField.setOrderDirection(sortDTO.getSort());
            return deSortField;
        }).collect(Collectors.toList());
    }

}
