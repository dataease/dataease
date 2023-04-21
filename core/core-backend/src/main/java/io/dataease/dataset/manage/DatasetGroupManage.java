package io.dataease.dataset.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import io.dataease.api.dataset.dto.DatasetNodeDTO;
import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.api.dataset.dto.DatasetTableFieldDTO;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.api.dataset.union.UnionDTO;
import io.dataease.api.dataset.vo.DatasetTreeNodeVO;
import io.dataease.dataset.dao.auto.entity.CoreDatasetGroup;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetGroupMapper;
import io.dataease.dataset.utils.TableUtils;
import io.dataease.engine.constant.ExtFieldConstant;
import io.dataease.exception.DEException;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import io.dataease.utils.JsonUtil;
import io.dataease.utils.TreeUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author Junjun
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class DatasetGroupManage {
    @Resource
    private CoreDatasetGroupMapper coreDatasetGroupMapper;
    @Resource
    private DatasetSQLManage datasetSQLManage;
    @Resource
    private DatasetDataManage datasetDataManage;
    @Resource
    private DatasetTableManage datasetTableManage;
    @Resource
    private DatasetTableFieldManage datasetTableFieldManage;

    public DatasetGroupInfoDTO save(DatasetGroupInfoDTO datasetGroupInfoDTO) throws Exception {
        checkName(datasetGroupInfoDTO);
        if (StringUtils.equalsIgnoreCase(datasetGroupInfoDTO.getNodeType(), "dataset")) {
            // get union sql
            Map<String, Object> sqlMap = datasetSQLManage.getUnionSQLForEdit(datasetGroupInfoDTO);
            if (ObjectUtils.isEmpty(sqlMap)) {
                DEException.throwException("table error");
            }
            String sql = (String) sqlMap.get("sql");
            datasetGroupInfoDTO.setUnionSql(sql);
            datasetGroupInfoDTO.setInfo(Objects.requireNonNull(JsonUtil.toJSONString(datasetGroupInfoDTO.getUnion())).toString());
        }
        // save dataset/group
        CoreDatasetGroup coreDatasetGroup = new CoreDatasetGroup();
        if (ObjectUtils.isEmpty(datasetGroupInfoDTO.getId())) {
            datasetGroupInfoDTO.setId(IDUtils.snowID());
            datasetGroupInfoDTO.setCreateBy("admin");// todo username
            datasetGroupInfoDTO.setCreateTime(System.currentTimeMillis());
            datasetGroupInfoDTO.setPid(datasetGroupInfoDTO.getPid() == null ? 0L : datasetGroupInfoDTO.getPid());
            BeanUtils.copyBean(coreDatasetGroup, datasetGroupInfoDTO);
            coreDatasetGroupMapper.insert(coreDatasetGroup);
        } else {
            if (Objects.equals(datasetGroupInfoDTO.getId(), datasetGroupInfoDTO.getPid())) {
                DEException.throwException("pid can not equal to id.");
            }
            BeanUtils.copyBean(coreDatasetGroup, datasetGroupInfoDTO);
            coreDatasetGroupMapper.updateById(coreDatasetGroup);
        }

        // node_type=dataset需要创建dataset_table和field
        if (StringUtils.equalsIgnoreCase(datasetGroupInfoDTO.getNodeType(), "dataset")) {
            List<Long> tableIds = new ArrayList<>();
            List<Long> fieldIds = new ArrayList<>();
            // 解析tree，保存
            saveTable(datasetGroupInfoDTO, datasetGroupInfoDTO.getUnion(), tableIds);
            saveField(datasetGroupInfoDTO, fieldIds);
            // 删除不要的table和field
            datasetTableManage.deleteByDatasetGroupUpdate(datasetGroupInfoDTO.getId(), tableIds);
            datasetTableFieldManage.deleteByDatasetGroupUpdate(datasetGroupInfoDTO.getId(), fieldIds);
        }
        return datasetGroupInfoDTO;
    }

    public void delete(Long id) {
        CoreDatasetGroup coreDatasetGroup = coreDatasetGroupMapper.selectById(id);
        if (ObjectUtils.isEmpty(coreDatasetGroup)) {
            return;
        }
        coreDatasetGroupMapper.deleteById(id);
        datasetTableManage.deleteByDatasetGroupDelete(id);
        datasetTableFieldManage.deleteByDatasetGroupDelete(id);

        QueryWrapper<CoreDatasetGroup> wrapper = new QueryWrapper<>();
        wrapper.eq("pid", id);
        List<CoreDatasetGroup> coreDatasetGroups = coreDatasetGroupMapper.selectList(wrapper);
        if (ObjectUtils.isNotEmpty(coreDatasetGroups)) {
            for (CoreDatasetGroup record : coreDatasetGroups) {
                delete(record.getId());
            }
        }
    }

    public List<DatasetTreeNodeVO> tree(DatasetNodeDTO datasetNodeDTO) {
        QueryWrapper<CoreDatasetGroup> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(datasetNodeDTO.getNodeType())) {
            wrapper.eq("node_type", datasetNodeDTO.getNodeType());
        }
        List<CoreDatasetGroup> coreDatasetTables = coreDatasetGroupMapper.selectList(wrapper);
        List<DatasetTreeNodeVO> collect = coreDatasetTables.stream().map(ele -> {
            DatasetTreeNodeVO vo = new DatasetTreeNodeVO();
            BeanUtils.copyBean(vo, ele);
            return vo;
        }).collect(Collectors.toList());
        return TreeUtils.mergeTree(collect);
    }

    public void checkName(DatasetGroupInfoDTO dto) {
        QueryWrapper<CoreDatasetGroup> wrapper = new QueryWrapper<>();
        if (ObjectUtils.isNotEmpty(dto.getPid())) {
            wrapper.eq("pid", dto.getPid());
        }
        if (StringUtils.isNotEmpty(dto.getName())) {
            wrapper.eq("name", dto.getName());
        }
        if (ObjectUtils.isNotEmpty(dto.getId())) {
            wrapper.ne("id", dto.getId());
        }
        if (ObjectUtils.isNotEmpty(dto.getLevel())) {
            wrapper.eq("level", dto.getLevel());
        }
        if (ObjectUtils.isNotEmpty(dto.getNodeType())) {
            wrapper.eq("node_type", dto.getNodeType());
        }
        List<CoreDatasetGroup> list = coreDatasetGroupMapper.selectList(wrapper);
        if (list.size() > 0) {
            throw new RuntimeException("I18N_DATASET_TABLE_EXIST");
        }
    }

    public void saveTable(DatasetGroupInfoDTO datasetGroupInfoDTO, List<UnionDTO> union, List<Long> tableIds) {
        // table和field均由前端生成id（如果没有id）
        Long datasetGroupId = datasetGroupInfoDTO.getId();
        if (ObjectUtils.isNotEmpty(union)) {
            for (UnionDTO unionDTO : union) {
                DatasetTableDTO currentDs = unionDTO.getCurrentDs();
                currentDs.setDatasetGroupId(datasetGroupId);
                datasetTableManage.save(currentDs);
                tableIds.add(currentDs.getId());

                saveTable(datasetGroupInfoDTO, unionDTO.getChildrenDs(), tableIds);
            }
        }
    }

    public void saveField(DatasetGroupInfoDTO datasetGroupInfoDTO, List<Long> fieldIds) {
        // table和field均由前端生成id（如果没有id）
        Long datasetGroupId = datasetGroupInfoDTO.getId();
        List<DatasetTableFieldDTO> allFields = datasetGroupInfoDTO.getAllFields();
        if (ObjectUtils.isEmpty(allFields)) {
            DEException.throwException("no fields");
        }
        // 获取内层union sql和字段
        Map<String, Object> map = datasetSQLManage.getUnionSQLForEdit(datasetGroupInfoDTO);
        List<DatasetTableFieldDTO> unionFields = (List<DatasetTableFieldDTO>) map.get("field");

        for (DatasetTableFieldDTO datasetTableFieldDTO : allFields) {
            DatasetTableFieldDTO dto = datasetTableFieldManage.selectById(datasetTableFieldDTO.getId());
            if (ObjectUtils.isEmpty(dto)) {
                if (Objects.equals(datasetTableFieldDTO.getExtField(), ExtFieldConstant.EXT_NORMAL)) {
                    for (DatasetTableFieldDTO fieldDTO : unionFields) {
                        if (Objects.equals(datasetTableFieldDTO.getDatasetTableId(), fieldDTO.getDatasetTableId())
                                && Objects.equals(datasetTableFieldDTO.getOriginName(), fieldDTO.getOriginName())) {
                            datasetTableFieldDTO.setDataeaseName(fieldDTO.getDataeaseName());
                            datasetTableFieldDTO.setFieldShortName(fieldDTO.getFieldShortName());
                        }
                    }
                }
                if (Objects.equals(datasetTableFieldDTO.getExtField(), ExtFieldConstant.EXT_CALC)) {
                    String dataeaseName = TableUtils.fieldNameShort(datasetTableFieldDTO.getId() + "_" + datasetTableFieldDTO.getOriginName());
                    datasetTableFieldDTO.setDataeaseName(dataeaseName);
                    datasetTableFieldDTO.setFieldShortName(dataeaseName);
                    datasetTableFieldDTO.setDeExtractType(datasetTableFieldDTO.getDeType());
                }
                datasetTableFieldDTO.setDatasetGroupId(datasetGroupId);
            } else {
                datasetTableFieldDTO.setDataeaseName(dto.getDataeaseName());
                datasetTableFieldDTO.setFieldShortName(dto.getFieldShortName());
            }
            datasetTableFieldDTO = datasetTableFieldManage.save(datasetTableFieldDTO);
            fieldIds.add(datasetTableFieldDTO.getId());
        }
    }

    public DatasetGroupInfoDTO get(Long id, String type) throws Exception {
        CoreDatasetGroup coreDatasetGroup = coreDatasetGroupMapper.selectById(id);
        if (coreDatasetGroup == null) {
            return null;
        }
        DatasetGroupInfoDTO dto = new DatasetGroupInfoDTO();
        BeanUtils.copyBean(dto, coreDatasetGroup);
        if (StringUtils.equalsIgnoreCase(dto.getNodeType(), "dataset")) {
            List<UnionDTO> unionDTOList = JsonUtil.parseList(coreDatasetGroup.getInfo(), new TypeReference<>() {
            });
            dto.setUnion(unionDTOList);

            // 获取field
            List<DatasetTableFieldDTO> dsFields = datasetTableFieldManage.selectByDatasetGroupId(id);
            List<DatasetTableFieldDTO> allFields = dsFields.stream().map(ele -> {
                DatasetTableFieldDTO datasetTableFieldDTO = new DatasetTableFieldDTO();
                BeanUtils.copyBean(datasetTableFieldDTO, ele);
                datasetTableFieldDTO.setFieldShortName(ele.getDataeaseName());
                return datasetTableFieldDTO;
            }).collect(Collectors.toList());
            dto.setAllFields(allFields);

            if ("preview".equalsIgnoreCase(type)) {
                // 请求数据
                Map<String, Object> map = datasetDataManage.previewDataWithLimit(dto, 0, 1000);
                // 获取data,sql
                Map<String, List> data = (Map<String, List>) map.get("data");
                String sql = (String) map.get("sql");
                dto.setData(data);
                dto.setSql(sql);
            }
        }
        return dto;
    }
}
