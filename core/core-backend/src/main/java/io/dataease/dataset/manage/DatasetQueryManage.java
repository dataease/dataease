package io.dataease.dataset.manage;

import com.fasterxml.jackson.core.type.TypeReference;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.api.dataset.union.UnionDTO;
import io.dataease.dao.auto.entity.CoreDatasetGroup;
import io.dataease.dao.auto.entity.CoreDatasetTable;
import io.dataease.dao.auto.entity.CoreDatasetTableField;
import io.dataease.dao.auto.repo.CoreDatasetTableFieldRepository;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetGroupRepository;
import io.dataease.dataset.utils.FieldUtils;
import io.dataease.extensions.datasource.dto.CalParam;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.datasource.dto.FieldGroupDTO;
import io.dataease.extensions.view.dto.SqlVariableDetails;
import io.dataease.system.manage.CoreUserManage;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.JsonUtil;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据集元数据只读查询服务：提供数据集分组、字段、SQL 变量等元数据读取。
 * 不依赖 DatasetDataManage / DatasetGroupManage，避免与数据执行层形成环。
 */
@Component
public class DatasetQueryManage {

    @Resource
    private CoreDatasetGroupRepository coreDatasetGroupRepository;
    @Resource
    private CoreDatasetTableFieldRepository coreDatasetTableFieldRepository;
    @Resource
    private CoreUserManage coreUserManage;
    @Resource
    private DatasetTableManage datasetTableManage;

    public DatasetGroupInfoDTO getForCount(Long id) throws Exception {
        CoreDatasetGroup coreDatasetGroup = coreDatasetGroupRepository.findById(id).orElse(null);
        if (coreDatasetGroup == null) {
            return null;
        }
        DatasetGroupInfoDTO dto = new DatasetGroupInfoDTO();
        BeanUtils.copyBean(dto, coreDatasetGroup);
        if (StringUtils.equalsIgnoreCase(dto.getNodeType(), "dataset")) {
            dto.setUnion(JsonUtil.parseList(coreDatasetGroup.getInfo(), new TypeReference<>() {
            }));
            // 获取field
            List<DatasetTableFieldDTO> dsFields = selectFieldsByDatasetGroupId(id);
            List<DatasetTableFieldDTO> allFields = dsFields.stream().map(ele -> {
                DatasetTableFieldDTO datasetTableFieldDTO = new DatasetTableFieldDTO();
                BeanUtils.copyBean(datasetTableFieldDTO, ele);
                datasetTableFieldDTO.setFieldShortName(ele.getDataeaseName());
                return datasetTableFieldDTO;
            }).collect(Collectors.toList());

            dto.setAllFields(allFields);
        }
        return dto;
    }

    public DatasetGroupInfoDTO getDatasetGroupInfoDTO(Long id, String type) throws Exception {
        CoreDatasetGroup coreDatasetGroup = coreDatasetGroupRepository.findById(id).orElse(null);
        if (coreDatasetGroup == null) {
            return null;
        }
        DatasetGroupInfoDTO dto = new DatasetGroupInfoDTO();
        BeanUtils.copyBean(dto, coreDatasetGroup);
        // get creator
        String userName = coreUserManage.getUserName(Long.valueOf(dto.getCreateBy()));
        if (StringUtils.isNotBlank(userName)) {
            dto.setCreator(userName);
        }
        String updateUserName = coreUserManage.getUserName(Long.valueOf(dto.getUpdateBy()));
        if (StringUtils.isNotBlank(updateUserName)) {
            dto.setUpdater(updateUserName);
        }
        dto.setUnionSql(null);
        if (StringUtils.equalsIgnoreCase(dto.getNodeType(), "dataset")) {
            List<UnionDTO> unionDTOList = JsonUtil.parseList(coreDatasetGroup.getInfo(), new TypeReference<>() {
            });
            dto.setUnion(unionDTOList);

            // 获取field
            List<DatasetTableFieldDTO> dsFields = selectFieldsByDatasetGroupId(id);
            List<DatasetTableFieldDTO> allFields = dsFields.stream().map(ele -> {
                DatasetTableFieldDTO datasetTableFieldDTO = new DatasetTableFieldDTO();
                BeanUtils.copyBean(datasetTableFieldDTO, ele);
                datasetTableFieldDTO.setFieldShortName(ele.getDataeaseName());
                return datasetTableFieldDTO;
            }).collect(Collectors.toList());

            dto.setAllFields(allFields);
        }
        return dto;
    }

    public List<SqlVariableDetails> getSqlParams(List<Long> ids) {
        List<SqlVariableDetails> list = new ArrayList<>();
        if (org.apache.commons.lang3.ObjectUtils.isEmpty(ids)) {
            return list;
        }
        TypeReference<List<SqlVariableDetails>> listTypeReference = new TypeReference<List<SqlVariableDetails>>() {
        };
        for (Long id : ids) {
            List<CoreDatasetTable> datasetTables = datasetTableManage.selectByDatasetGroupId(id);
            for (CoreDatasetTable datasetTable : datasetTables) {
                if (StringUtils.isNotEmpty(datasetTable.getSqlVariableDetails())) {
                    List<SqlVariableDetails> defaultsSqlVariableDetails = JsonUtil.parseList(datasetTable.getSqlVariableDetails(), listTypeReference);
                    if (CollectionUtils.isNotEmpty(defaultsSqlVariableDetails)) {
                        List<String> fullName = new ArrayList<>();
                        geFullName(id, fullName);
                        Collections.reverse(fullName);
                        List<String> finalFullName = fullName;
                        defaultsSqlVariableDetails.forEach(sqlVariableDetails -> {
                            sqlVariableDetails.setDatasetGroupId(id);
                            sqlVariableDetails.setDatasetTableId(datasetTable.getId());
                            sqlVariableDetails.setDatasetFullName(String.join("/", finalFullName));
                        });
                    }

                    list.addAll(defaultsSqlVariableDetails);
                }
            }
        }
        list.forEach(sqlVariableDetail -> {
            sqlVariableDetail.setId(sqlVariableDetail.getDatasetTableId() + "|DE|" + sqlVariableDetail.getVariableName());
            sqlVariableDetail.setDeType(FieldUtils.transType2DeType(sqlVariableDetail.getType().get(0).contains("DATETIME") ? "DATETIME" : sqlVariableDetail.getType().get(0)));
        });
        return list;
    }

    private List<DatasetTableFieldDTO> selectFieldsByDatasetGroupId(Long id) {
        Specification<CoreDatasetTableField> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("datasetGroupId"), id));
            predicates.add(cb.isTrue(root.get("checked")));
            predicates.add(cb.isNull(root.get("chartId")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return coreDatasetTableFieldRepository.findAll(spec).stream().map(ele -> {
            DatasetTableFieldDTO dto = new DatasetTableFieldDTO();
            BeanUtils.copyBean(dto, ele);
            if (StringUtils.isNotEmpty(ele.getParams())) {
                TypeReference<List<CalParam>> tokenType = new TypeReference<>() {
                };
                dto.setParams(JsonUtil.parseList(ele.getParams(), tokenType));
            }
            if (StringUtils.isNotEmpty(ele.getGroupList())) {
                TypeReference<List<FieldGroupDTO>> groupTokenType = new TypeReference<>() {
                };
                dto.setGroupList(JsonUtil.parseList(ele.getGroupList(), groupTokenType));
            }
            return dto;
        }).collect(Collectors.toList());
    }

    public void geFullName(Long pid, List<String> fullName) {
        CoreDatasetGroup parent = coreDatasetGroupRepository.findById(pid).orElse(null);// 查找父级folder
        if (parent == null) {
            return;
        }
        fullName.add(parent.getName());
        if (parent.getId().equals(parent.getPid())) {
            return;
        }
        if (parent.getPid() != null && parent.getPid() != 0) {
            geFullName(parent.getPid(), fullName);
        }
    }
}
