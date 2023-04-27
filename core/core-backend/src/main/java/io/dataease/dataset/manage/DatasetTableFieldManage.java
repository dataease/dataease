package io.dataease.dataset.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.dataset.dto.DatasetTableFieldDTO;
import io.dataease.dataset.dao.auto.entity.CoreDatasetTableField;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetTableFieldMapper;
import io.dataease.dataset.utils.FieldUtils;
import io.dataease.dataset.utils.TableUtils;
import io.dataease.engine.constant.ExtFieldConstant;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author Junjun
 */
@Component
public class DatasetTableFieldManage {
    @Resource
    private CoreDatasetTableFieldMapper coreDatasetTableFieldMapper;

    public void save(CoreDatasetTableField coreDatasetTableField) {
        if (ObjectUtils.isEmpty(coreDatasetTableField.getId())) {
            coreDatasetTableField.setId(IDUtils.snowID());
            coreDatasetTableFieldMapper.insert(coreDatasetTableField);
        } else {
            coreDatasetTableFieldMapper.updateById(coreDatasetTableField);
        }
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
}
