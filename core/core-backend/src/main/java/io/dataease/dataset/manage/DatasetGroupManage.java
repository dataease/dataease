package io.dataease.dataset.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.dataset.dto.DatasetNodeDTO;
import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.api.dataset.dto.DatasetTableFieldDTO;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.api.dataset.union.UnionDTO;
import io.dataease.api.dataset.vo.DatasetTreeNodeVO;
import io.dataease.dataset.dao.auto.entity.CoreDatasetGroup;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetGroupMapper;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import io.dataease.utils.TreeUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
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
        // get union sql
        Map<String, Object> sqlMap = datasetSQLManage.getUnionSQLForEdit(datasetGroupInfoDTO);
        String sql = (String) sqlMap.get("sql");
        datasetGroupInfoDTO.setUnionSql(sql);
        // save dataset group
        CoreDatasetGroup coreDatasetGroup = new CoreDatasetGroup();
        if (ObjectUtils.isEmpty(datasetGroupInfoDTO.getId())) {
            datasetGroupInfoDTO.setId(IDUtils.snowID());
            datasetGroupInfoDTO.setCreateBy("admin");// todo username
            datasetGroupInfoDTO.setCreateTime(System.currentTimeMillis());
            BeanUtils.copyBean(coreDatasetGroup, datasetGroupInfoDTO);
            coreDatasetGroupMapper.insert(coreDatasetGroup);
        } else {
            BeanUtils.copyBean(coreDatasetGroup, datasetGroupInfoDTO);
            coreDatasetGroupMapper.updateById(coreDatasetGroup);
        }

        // node_type=dataset需要创建dataset_table和field
        if (StringUtils.equalsIgnoreCase(datasetGroupInfoDTO.getNodeType(), "dataset")) {
            // 解析tree，保存
            saveTableAndField(datasetGroupInfoDTO, datasetGroupInfoDTO.getUnion());
        }

        return datasetGroupInfoDTO;
    }

    public void delete(String id) {
        coreDatasetGroupMapper.deleteById(id);
    }

    public List<DatasetTreeNodeVO> tree(DatasetNodeDTO datasetNodeDTO) {
        QueryWrapper<CoreDatasetGroup> wrapper = new QueryWrapper<>();
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

    public void saveTableAndField(DatasetGroupInfoDTO datasetGroupInfoDTO, List<UnionDTO> union) {
        // table和field均由前端生成id（如果没有id）
        Long datasetGroupId = datasetGroupInfoDTO.getId();
        if (ObjectUtils.isNotEmpty(union)) {
            for (UnionDTO unionDTO : union) {
                DatasetTableDTO currentDs = unionDTO.getCurrentDs();
                currentDs.setDatasetGroupId(datasetGroupId);
                datasetTableManage.save(currentDs);

                List<DatasetTableFieldDTO> currentDsFields = unionDTO.getCurrentDsFields();
                if (ObjectUtils.isNotEmpty(currentDsFields)) {
                    for (DatasetTableFieldDTO datasetTableFieldDTO : currentDsFields) {
                        datasetTableFieldDTO.setDatasourceId(currentDs.getDatasourceId());
                        datasetTableFieldDTO.setDatasetGroupId(datasetGroupId);
                        datasetTableFieldDTO.setDatasetTableId(currentDs.getId());
                        datasetTableFieldManage.save(datasetTableFieldDTO);
                    }
                }

                saveTableAndField(datasetGroupInfoDTO, unionDTO.getChildrenDs());
            }
        }
    }
}
