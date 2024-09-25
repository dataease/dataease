package io.dataease.dataset.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.dataset.dao.auto.entity.CoreDatasetTableField;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetTableFieldMapper;
import io.dataease.dataset.utils.TableUtils;
import io.dataease.engine.constant.ExtFieldConstant;
import io.dataease.engine.func.FunctionConstant;
import io.dataease.engine.utils.Utils;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.api.PluginManageApi;
import io.dataease.extensions.datasource.dto.CalParam;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.model.SQLObj;
import io.dataease.extensions.view.dto.ColumnPermissionItem;
import io.dataease.i18n.Translator;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import io.dataease.utils.JsonUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    private DatasetSQLManage datasetSQLManage;
    @Resource
    private DatasetGroupManage datasetGroupManage;
    @Autowired(required = false)
    private PluginManageApi pluginManage;

    public void save(CoreDatasetTableField coreDatasetTableField) {
        checkNameLength(coreDatasetTableField.getName());
        if (ObjectUtils.isEmpty(coreDatasetTableField.getId())) {
            coreDatasetTableField.setId(IDUtils.snowID());
            coreDatasetTableFieldMapper.insert(coreDatasetTableField);
        } else {
            coreDatasetTableFieldMapper.updateById(coreDatasetTableField);
        }
    }

    public DatasetTableFieldDTO chartFieldSave(DatasetTableFieldDTO datasetTableFieldDTO) {
        checkNameLength(datasetTableFieldDTO.getName());
        CoreDatasetTableField coreDatasetTableField = coreDatasetTableFieldMapper.selectById(datasetTableFieldDTO.getId());
        QueryWrapper<CoreDatasetTableField> wrapper = new QueryWrapper<>();
        wrapper.eq("name", datasetTableFieldDTO.getName());
        wrapper.eq("chart_id", datasetTableFieldDTO.getChartId());
        if (ObjectUtils.isNotEmpty(coreDatasetTableField)) {
            wrapper.ne("id", datasetTableFieldDTO.getId());
        }
        List<CoreDatasetTableField> fields = coreDatasetTableFieldMapper.selectList(wrapper);
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
        CoreDatasetTableField coreDatasetTableField = coreDatasetTableFieldMapper.selectById(datasetTableFieldDTO.getId());
        CoreDatasetTableField record = transDTO2Record(datasetTableFieldDTO);
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

    public List<DatasetTableFieldDTO> getChartCalcFields(Long chartId) {
        QueryWrapper<CoreDatasetTableField> wrapper = new QueryWrapper<>();
        wrapper.eq("chart_id", chartId);
        return transDTO(coreDatasetTableFieldMapper.selectList(wrapper));
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
        wrapper.eq("checked", true);
        wrapper.isNull("chart_id");
        return transDTO(coreDatasetTableFieldMapper.selectList(wrapper));
    }

    public Map<String, List<DatasetTableFieldDTO>> selectByDatasetGroupIds(List<Long> ids) {
        Map<String, List<DatasetTableFieldDTO>> map = new HashMap<>();
        for (Long id : ids) {
            QueryWrapper<CoreDatasetTableField> wrapper = new QueryWrapper<>();
            wrapper.eq("dataset_group_id", id);
            wrapper.eq("checked", true);
            wrapper.isNull("chart_id");
            wrapper.eq("ext_field", 0);
            map.put(String.valueOf(id), transDTO(coreDatasetTableFieldMapper.selectList(wrapper)));
        }
        return map;
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
        wrapper.eq("checked", true);
        List<DatasetTableFieldDTO> list = transDTO(coreDatasetTableFieldMapper.selectList(wrapper));
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
            DEException.throwException("跨源数据集不支持该功能");
        }

        QueryWrapper<CoreDatasetTableField> wrapper = new QueryWrapper<>();
        wrapper.eq("dataset_group_id", id);
        wrapper.eq("checked", true);
        wrapper.eq("ext_field", 0);
        List<DatasetTableFieldDTO> list = transDTO(coreDatasetTableFieldMapper.selectList(wrapper));

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
                        String originField = Utils.calcFieldRegex(ele.getOriginName(), tableObj, fields, true, null, Utils.mergeParam(Utils.getParams(fields), null), pluginManage);
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
        return tmp;
    }

    public List<DatasetTableFieldDTO> transDTO(List<CoreDatasetTableField> list) {
        return list.stream().map(ele -> {
            DatasetTableFieldDTO dto = new DatasetTableFieldDTO();
            if (ele == null) return null;
            BeanUtils.copyBean(dto, ele);
            if (StringUtils.isNotEmpty(ele.getParams())) {
                TypeReference<List<CalParam>> tokenType = new TypeReference<>() {
                };
                List<CalParam> calParams = JsonUtil.parseList(ele.getParams(), tokenType);
                dto.setParams(calParams);
            }
            return dto;
        }).collect(Collectors.toList());
    }

    private CoreDatasetTableField transDTO2Record(DatasetTableFieldDTO dto) {
        CoreDatasetTableField record = new CoreDatasetTableField();
        BeanUtils.copyBean(record, dto);
        if (ObjectUtils.isNotEmpty(dto.getParams())) {
            record.setParams(JsonUtil.toJSONString(dto.getParams()).toString());
        }
        return record;
    }

    private void checkNameLength(String name) {
        if (name != null && name.length() > 100) {
            DEException.throwException(Translator.get("i18n_name_limit_100"));
        }
    }
}
