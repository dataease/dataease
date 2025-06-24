package io.dataease.dataset.manage;

import com.fasterxml.jackson.core.type.TypeReference;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.dao.auto.entity.CoreDatasetTableField;
import io.dataease.dao.auto.repo.CoreDatasetTableFieldRepository;
import io.dataease.dataset.utils.DatasetUtils;
import io.dataease.dataset.utils.TableUtils;
import io.dataease.engine.constant.ExtFieldConstant;
import io.dataease.engine.func.FunctionConstant;
import io.dataease.engine.utils.Utils;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.api.PluginManageApi;
import io.dataease.extensions.datasource.dto.CalParam;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.dto.FieldGroupDTO;
import io.dataease.extensions.datasource.model.SQLObj;
import io.dataease.extensions.view.dto.ColumnPermissionItem;
import io.dataease.i18n.Translator;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import io.dataease.utils.JsonUtil;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
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
    private CoreDatasetTableFieldRepository coreDatasetTableFieldRepository;
    @Resource
    private PermissionManage permissionManage;
    @Resource
    private DatasetSQLManage datasetSQLManage;
    @Resource
    private DatasetGroupManage datasetGroupManage;
    @Autowired(required = false)
    private PluginManageApi pluginManage;

    public void save(CoreDatasetTableField coreDatasetTableField) {
        checkNameLength(coreDatasetTableField.getName());
        if (ObjectUtils.isEmpty(coreDatasetTableField.getId())) {
            coreDatasetTableField.setId(IDUtils.snowID());
            coreDatasetTableFieldRepository.saveAndFlush(coreDatasetTableField);
        } else {
            coreDatasetTableFieldRepository.saveAndFlush(coreDatasetTableField);
        }
    }

    public DatasetTableFieldDTO chartFieldSave(DatasetTableFieldDTO datasetTableFieldDTO) {
        checkNameLength(datasetTableFieldDTO.getName());
        CoreDatasetTableField coreDatasetTableField = coreDatasetTableFieldRepository.findById(datasetTableFieldDTO.getId()).orElse(null);
        Specification<CoreDatasetTableField> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("name"), datasetTableFieldDTO.getName()));
            predicates.add(cb.equal(root.get("chartId"), datasetTableFieldDTO.getChartId()));
            if (coreDatasetTableField != null && coreDatasetTableField.getId() != null) {
                predicates.add(cb.notEqual(root.get("id"), datasetTableFieldDTO.getId()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<CoreDatasetTableField> fields = coreDatasetTableFieldRepository.findAll(spec);
        if (ObjectUtils.isNotEmpty(fields)) {
            DEException.throwException(Translator.get("i18n_field_name_duplicated"));
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
        checkNameLength(datasetTableFieldDTO.getName());
        CoreDatasetTableField coreDatasetTableField = coreDatasetTableFieldRepository.findById(datasetTableFieldDTO.getId()).orElse(null);
        CoreDatasetTableField record = transDTO2Record(datasetTableFieldDTO);
        if (ObjectUtils.isEmpty(record.getDataeaseName())) {
            String n = TableUtils.fieldNameShort(record.getId() + "");
            record.setFieldShortName(n);
            record.setDataeaseName(n);
        }
        if (ObjectUtils.isEmpty(coreDatasetTableField)) {
            coreDatasetTableFieldRepository.saveAndFlush(record);
        } else {
            coreDatasetTableFieldRepository.saveAndFlush(record);
        }
        return datasetTableFieldDTO;
    }

    public DatasetTableFieldDTO saveField(DatasetTableFieldDTO datasetTableFieldDTO) {
        CoreDatasetTableField record = new CoreDatasetTableField();
        if (ObjectUtils.isEmpty(datasetTableFieldDTO.getId())) {
            datasetTableFieldDTO.setId(IDUtils.snowID());
            BeanUtils.copyBean(record, datasetTableFieldDTO);
            coreDatasetTableFieldRepository.saveAndFlush(record);
        } else {
            BeanUtils.copyBean(record, datasetTableFieldDTO);
            coreDatasetTableFieldRepository.saveAndFlush(record);
        }
        return datasetTableFieldDTO;
    }

    public List<DatasetTableFieldDTO> getChartCalcFields(Long chartId) {
        return transDTO(coreDatasetTableFieldRepository.findByChartId(chartId));
    }

    public void deleteById(Long id) {
        coreDatasetTableFieldRepository.deleteById(id);
    }

    public void deleteByDatasetTableUpdate(Long datasetTableId, List<Long> fieldIds) {
        if (!CollectionUtils.isEmpty(fieldIds)) {
            coreDatasetTableFieldRepository.deleteByDatasetTableIdAndNotInFieldIds(datasetTableId, fieldIds);
        }
    }

    public void deleteByDatasetGroupUpdate(Long datasetGroupId, List<Long> fieldIds) {
        if (!CollectionUtils.isEmpty(fieldIds)) {
            coreDatasetTableFieldRepository.deleteByDatasetGroupIdAndNotInFieldIds(datasetGroupId, fieldIds);
        }
    }

    public void deleteByDatasetGroupDelete(Long datasetGroupId) {
        coreDatasetTableFieldRepository.deleteByDatasetGroupId(datasetGroupId);
    }

    public void deleteByChartId(Long chartId) {
        coreDatasetTableFieldRepository.deleteByChartId(chartId);
    }

    public List<DatasetTableFieldDTO> selectByDatasetTableId(Long id) {
        return transDTO(coreDatasetTableFieldRepository.findByDatasetTableId(id));
    }

    public List<DatasetTableFieldDTO> selectByDatasetGroupId(Long id) {
        Specification<CoreDatasetTableField> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("datasetGroupId"), id));
            predicates.add(cb.isTrue(root.get("checked")));
            predicates.add(cb.isNull(root.get("chartId")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return transDTO(coreDatasetTableFieldRepository.findAll(spec));
    }

    public Map<String, List<DatasetTableFieldDTO>> selectByDatasetGroupIds(List<Long> ids) {
        Map<String, List<DatasetTableFieldDTO>> map = new HashMap<>();
        for (Long id : ids) {
            Specification<CoreDatasetTableField> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("datasetGroupId"), id));
                predicates.add(cb.isTrue(root.get("checked")));
                predicates.add(cb.isNull(root.get("chartId")));
                predicates.add(cb.equal(root.get("extField"), 0));
                return cb.and(predicates.toArray(new Predicate[0]));
            };
            map.put(String.valueOf(id), transDTO(coreDatasetTableFieldRepository.findAll(spec)));
        }
        return map;
    }

    public List<DatasetTableFieldDTO> selectByFieldIds(List<Long> ids) {
        return transDTO(coreDatasetTableFieldRepository.findAllById(ids));
    }

    public DatasetTableFieldDTO selectById(Long id) {
        CoreDatasetTableField coreDatasetTableField = coreDatasetTableFieldRepository.findById(id).orElse(null);
        if (coreDatasetTableField == null) return null;
        return transObj(coreDatasetTableField);
    }

    /**
     * 返回维度、指标列表
     *
     * @return
     */
    public Map<String, List<DatasetTableFieldDTO>> listByDQ(Long id) {
        Specification<CoreDatasetTableField> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("datasetGroupId"), id));
            predicates.add(cb.isTrue(root.get("checked")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<DatasetTableFieldDTO> list = transDTO(coreDatasetTableFieldRepository.findAll(spec));
        List<DatasetTableFieldDTO> dimensionList = list.stream().filter(ele -> StringUtils.equalsIgnoreCase(ele.getGroupType(), "d")).collect(Collectors.toList());
        List<DatasetTableFieldDTO> quotaList = list.stream().filter(ele -> StringUtils.equalsIgnoreCase(ele.getGroupType(), "q")).collect(Collectors.toList());
        Map<String, List<DatasetTableFieldDTO>> map = new LinkedHashMap<>();
        map.put("dimensionList", dimensionList);
        map.put("quotaList", quotaList);
        return map;
    }

    public Map<String, List<DatasetTableFieldDTO>> copilotFields(Long id) throws Exception {
        DatasetGroupInfoDTO datasetGroupInfoDTO = datasetGroupManage.getDatasetGroupInfoDTO(id, null);
        Map<String, Object> sqlMap = datasetSQLManage.getUnionSQLForEdit(datasetGroupInfoDTO, null);
        Map<Long, DatasourceSchemaDTO> dsMap = (Map<Long, DatasourceSchemaDTO>) sqlMap.get("dsMap");
        boolean crossDs = Utils.isCrossDs(dsMap);
        if (crossDs) {
            DEException.throwException(Translator.get("i18n_dataset_cross_error"));
        }
        if (!isCopilotSupport(dsMap)) {
            DEException.throwException(Translator.get("i18n_copilot_ds"));
        }
        Specification<CoreDatasetTableField> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("datasetGroupId"), id));
            predicates.add(cb.isTrue(root.get("checked")));
            predicates.add(cb.equal(root.get("extField"), 0));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<DatasetTableFieldDTO> list = transDTO(coreDatasetTableFieldRepository.findAll(spec));
        Map<String, ColumnPermissionItem> desensitizationList = new HashMap<>();
        list = permissionManage.filterColumnPermissions(list, desensitizationList, id, null);

        List<DatasetTableFieldDTO> dimensionList = list.stream().filter(ele -> StringUtils.equalsIgnoreCase(ele.getGroupType(), "d")).collect(Collectors.toList());
        List<DatasetTableFieldDTO> quotaList = list.stream().filter(ele -> StringUtils.equalsIgnoreCase(ele.getGroupType(), "q")).collect(Collectors.toList());
        Map<String, List<DatasetTableFieldDTO>> map = new LinkedHashMap<>();
        map.put("dimensionList", dimensionList);
        map.put("quotaList", quotaList);
        return map;
    }

    public List<DatasetTableFieldDTO> listFieldsWithPermissions(Long id) {
        List<DatasetTableFieldDTO> fields = selectByDatasetGroupId(id);
        Map<String, ColumnPermissionItem> desensitizationList = new HashMap<>();
        Long userId = AuthUtils.getUser() == null ? null : AuthUtils.getUser().getUserId();
        List<DatasetTableFieldDTO> tmp = permissionManage
                .filterColumnPermissions(fields, desensitizationList, id, userId)
                .stream()
                .sorted(Comparator.comparing(DatasetTableFieldDTO::getGroupType))
                .toList();
        tmp.forEach(ele -> ele.setDesensitized(desensitizationList.containsKey(ele.getDataeaseName())));
        return tmp;
    }

    public List<DatasetTableFieldDTO> listFieldsWithPermissionsRemoveAgg(Long id) {
        List<DatasetTableFieldDTO> fields = selectByDatasetGroupId(id);
        Map<String, ColumnPermissionItem> desensitizationList = new HashMap<>();
        Long userId = AuthUtils.getUser() == null ? null : AuthUtils.getUser().getUserId();
        SQLObj tableObj = new SQLObj();
        tableObj.setTableAlias("");
        List<DatasetTableFieldDTO> tmp = permissionManage
                .filterColumnPermissions(fields, desensitizationList, id, userId)
                .stream()
                .filter(ele -> {
                    boolean flag = true;
                    if (Objects.equals(ele.getExtField(), ExtFieldConstant.EXT_CALC)) {
                        String originField = Utils.calcFieldRegex(ele, tableObj, fields, true, null, Utils.mergeParam(Utils.getParams(fields), null), pluginManage);
                        for (String func : FunctionConstant.AGG_FUNC) {
                            if (Utils.matchFunction(func, originField)) {
                                flag = false;
                                break;
                            }
                        }
                    }
                    return flag;
                })
                .sorted(Comparator.comparing(DatasetTableFieldDTO::getGroupType))
                .toList();
        tmp.forEach(ele -> ele.setDesensitized(desensitizationList.containsKey(ele.getDataeaseName())));
        DatasetUtils.listEncode(tmp);
        return tmp;
    }

    public DatasetTableFieldDTO transObj(CoreDatasetTableField ele) {
        DatasetTableFieldDTO dto = new DatasetTableFieldDTO();
        if (ele == null) return null;
        BeanUtils.copyBean(dto, ele);
        if (StringUtils.isNotEmpty(ele.getParams())) {
            TypeReference<List<CalParam>> tokenType = new TypeReference<>() {
            };
            List<CalParam> calParams = JsonUtil.parseList(ele.getParams(), tokenType);
            dto.setParams(calParams);
        }
        if (StringUtils.isNotEmpty(ele.getGroupList())) {
            TypeReference<List<FieldGroupDTO>> groupTokenType = new TypeReference<>() {
            };
            List<FieldGroupDTO> fieldGroups = JsonUtil.parseList(ele.getGroupList(), groupTokenType);
            dto.setGroupList(fieldGroups);
        }
        return dto;
    }

    public List<DatasetTableFieldDTO> transDTO(List<CoreDatasetTableField> list) {
        if (!CollectionUtils.isEmpty(list)) {
            return list.stream().map(this::transObj).collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    private CoreDatasetTableField transDTO2Record(DatasetTableFieldDTO dto) {
        CoreDatasetTableField record = new CoreDatasetTableField();
        BeanUtils.copyBean(record, dto);
        if (ObjectUtils.isNotEmpty(dto.getParams())) {
            record.setParams(JsonUtil.toJSONString(dto.getParams()).toString());
        }
        if (ObjectUtils.isNotEmpty(dto.getGroupList())) {
            record.setGroupList(JsonUtil.toJSONString(dto.getGroupList()).toString());
        }
        return record;
    }

    private void checkNameLength(String name) {
        if (name != null && name.length() > 100) {
            DEException.throwException(Translator.get("i18n_name_limit_100"));
        }
    }

    public boolean isCopilotSupport(Map<Long, DatasourceSchemaDTO> dsMap) {
        DatasourceSchemaDTO value = dsMap.entrySet().iterator().next().getValue();
        return StringUtils.equalsIgnoreCase(value.getType(), "mysql");
    }

    public List<DatasetTableFieldDTO> queryTableFieldWithViewId(Long viewId) {
        return coreDatasetTableFieldRepository.findByViewId(viewId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 使用BeanUtils进行单个对象转换
    private DatasetTableFieldDTO convertToDto(CoreDatasetTableField field) {
        DatasetTableFieldDTO dto = new DatasetTableFieldDTO();
        BeanUtils.copyBean(dto, field);
        return dto;
    }
}
