package io.dataease.dataset.manage;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.api.dataset.union.UnionDTO;
import io.dataease.api.dataset.vo.DataSetBarVO;
import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.api.permissions.auth.dto.BusiResourceCreator;
import io.dataease.api.permissions.auth.dto.BusiResourceEditor;
import io.dataease.api.permissions.auth.dto.BusiResourceMover;
import io.dataease.dataset.dao.auto.entity.CoreDatasetGroup;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetGroupMapper;
import io.dataease.dataset.dao.ext.mapper.CoreDataSetExtMapper;
import io.dataease.dataset.dao.ext.po.DataSetNodePO;
import io.dataease.dataset.dto.DataSetNodeBO;
import io.dataease.dataset.utils.TableUtils;
import io.dataease.dto.dataset.DatasetTableFieldDTO;
import io.dataease.engine.constant.ExtFieldConstant;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import io.dataease.utils.*;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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

    @Autowired(required = false)
    private InteractiveAuthApi interactiveAuthApi;

    @Resource
    private CoreDataSetExtMapper coreDataSetExtMapper;

    private static final String leafType = "dataset";

    public DatasetGroupInfoDTO save(DatasetGroupInfoDTO datasetGroupInfoDTO) throws Exception {
        checkName(datasetGroupInfoDTO);
        if (StringUtils.equalsIgnoreCase(datasetGroupInfoDTO.getNodeType(), leafType)) {
            // get union sql
            Map<String, Object> sqlMap = datasetSQLManage.getUnionSQLForEdit(datasetGroupInfoDTO);
            if (ObjectUtils.isNotEmpty(sqlMap)) {
                String sql = (String) sqlMap.get("sql");
                datasetGroupInfoDTO.setUnionSql(sql);
                datasetGroupInfoDTO.setInfo(Objects.requireNonNull(JsonUtil.toJSONString(datasetGroupInfoDTO.getUnion())).toString());
            }
        }
        // save dataset/group
        long time = System.currentTimeMillis();
        CoreDatasetGroup coreDatasetGroup = new CoreDatasetGroup();
        if (ObjectUtils.isEmpty(datasetGroupInfoDTO.getId())) {
            datasetGroupInfoDTO.setId(IDUtils.snowID());
            datasetGroupInfoDTO.setCreateBy(null);// todo username
            datasetGroupInfoDTO.setCreateTime(time);
            datasetGroupInfoDTO.setLastUpdateTime(time);
            datasetGroupInfoDTO.setPid(datasetGroupInfoDTO.getPid() == null ? 0L : datasetGroupInfoDTO.getPid());
            BeanUtils.copyBean(coreDatasetGroup, datasetGroupInfoDTO);
            coreDatasetGroupMapper.insert(coreDatasetGroup);
            if (ObjectUtils.isNotEmpty(interactiveAuthApi)) {
                BusiResourceCreator creator = new BusiResourceCreator();
                creator.setId(datasetGroupInfoDTO.getId());
                creator.setPid(datasetGroupInfoDTO.getPid());
                creator.setFlag(leafType);
                creator.setName(datasetGroupInfoDTO.getName());
                creator.setLeaf(StringUtils.equals(leafType, datasetGroupInfoDTO.getNodeType()));
                interactiveAuthApi.saveResource(creator);
            }
        } else {
            if (Objects.equals(datasetGroupInfoDTO.getId(), datasetGroupInfoDTO.getPid())) {
                DEException.throwException(Translator.get("i18n_pid_not_eq_id"));
            }
            CoreDatasetGroup sourceData = coreDatasetGroupMapper.selectById(datasetGroupInfoDTO.getId());
            BeanUtils.copyBean(coreDatasetGroup, datasetGroupInfoDTO);
            coreDatasetGroup.setLastUpdateTime(time);
            coreDatasetGroupMapper.updateById(coreDatasetGroup);
            if (ObjectUtils.isNotEmpty(interactiveAuthApi) && ObjectUtils.isNotEmpty(sourceData) && (!StringUtils.equals(sourceData.getName(), coreDatasetGroup.getName()) || !sourceData.getPid().equals(coreDatasetGroup.getPid()
            ))) {
                BusiResourceEditor editor = new BusiResourceEditor();
                editor.setId(coreDatasetGroup.getId());
                editor.setName(coreDatasetGroup.getName());
                editor.setFlag(leafType);
                interactiveAuthApi.editResource(editor);
            }
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

    @Transactional
    public DatasetGroupInfoDTO move(DatasetGroupInfoDTO datasetGroupInfoDTO) {
        checkName(datasetGroupInfoDTO);
        // save dataset/group
        long time = System.currentTimeMillis();
        CoreDatasetGroup coreDatasetGroup = new CoreDatasetGroup();
        if (Objects.equals(datasetGroupInfoDTO.getId(), datasetGroupInfoDTO.getPid())) {
            DEException.throwException(Translator.get("i18n_pid_not_eq_id"));
        }
        CoreDatasetGroup sourceData = coreDatasetGroupMapper.selectById(datasetGroupInfoDTO.getId());
        BeanUtils.copyBean(coreDatasetGroup, datasetGroupInfoDTO);
        coreDatasetGroup.setLastUpdateTime(time);
        coreDatasetGroupMapper.updateById(coreDatasetGroup);
        if (ObjectUtils.isNotEmpty(interactiveAuthApi) && ObjectUtils.isNotEmpty(sourceData)) {
            BusiResourceMover mover = new BusiResourceMover();
            mover.setId(sourceData.getId());
            mover.setPid(coreDatasetGroup.getPid());
            interactiveAuthApi.moveResource(mover);
        }
        return datasetGroupInfoDTO;
    }

    public void delete(Long id) {
        CoreDatasetGroup coreDatasetGroup = coreDatasetGroupMapper.selectById(id);
        if (ObjectUtils.isEmpty(coreDatasetGroup)) {
            return;
        }
        boolean delAuthMatch = false;
        if (ObjectUtils.isNotEmpty(interactiveAuthApi)) {
            delAuthMatch = interactiveAuthApi.checkDel(id);
        }
        if (!delAuthMatch) return;
        CommonBeanFactory.getBean(this.getClass()).recursionDel(id);
        if (ObjectUtils.isNotEmpty(interactiveAuthApi)) {
            interactiveAuthApi.delResource(id);
        }
    }

    public void recursionDel(Long id) {
        coreDatasetGroupMapper.deleteById(id);
        datasetTableManage.deleteByDatasetGroupDelete(id);
        datasetTableFieldManage.deleteByDatasetGroupDelete(id);

        QueryWrapper<CoreDatasetGroup> wrapper = new QueryWrapper<>();
        wrapper.eq("pid", id);
        List<CoreDatasetGroup> coreDatasetGroups = coreDatasetGroupMapper.selectList(wrapper);
        if (ObjectUtils.isNotEmpty(coreDatasetGroups)) {
            for (CoreDatasetGroup record : coreDatasetGroups) {
                recursionDel(record.getId());
            }
        }
    }


    public List<BusiNodeVO> tree(BusiNodeRequest request) {
        request.setBusyFlag(leafType);
        if (ObjectUtils.isNotEmpty(interactiveAuthApi)) {
            return interactiveAuthApi.resource(request);
        }
        QueryWrapper queryWrapper = new QueryWrapper();
        if (ObjectUtils.isNotEmpty(request.getLeaf())) {
            queryWrapper.eq("node_type", request.getLeaf() ? "dataset" : "folder");
        }

        queryWrapper.orderByDesc("create_time");
        List<DataSetNodePO> pos = coreDataSetExtMapper.query(queryWrapper);
        List<DataSetNodeBO> nodes = new ArrayList<>();
        if (ObjectUtils.isEmpty(request.getLeaf()) || !request.getLeaf()) nodes.add(rootNode());
        List<DataSetNodeBO> bos = pos.stream().map(this::convert).toList();
        if (CollectionUtil.isNotEmpty(bos)) {
            nodes.addAll(bos);
        }
        return TreeUtils.mergeTree(nodes, BusiNodeVO.class, false);
    }

    public DataSetBarVO queryBarInfo(Long id) {
        return coreDataSetExtMapper.queryBarInfo(id);
    }

    private DataSetNodeBO rootNode() {
        return new DataSetNodeBO(0L, "root", false, 3, -1L, 0);
    }

    private DataSetNodeBO convert(DataSetNodePO po) {
        return new DataSetNodeBO(po.getId(), po.getName(), StringUtils.equals(po.getNodeType(), leafType), 3, po.getPid(), 0);
    }
    /*public List tree(DatasetNodeDTO datasetNodeDTO) {
        if (ObjectUtils.isNotEmpty(interactiveAuthApi)) {
            List<BusiNodeVO> resource = interactiveAuthApi.resource(leafType);
            if (StringUtils.equalsIgnoreCase("folder", datasetNodeDTO.getNodeType())) {
                filterNode(resource);
            }
            return resource;
        }
        QueryWrapper<CoreDatasetGroup> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(datasetNodeDTO.getNodeType())) {
            wrapper.eq("node_type", datasetNodeDTO.getNodeType());
        }
        List<CoreDatasetGroup> coreDatasetTables = coreDatasetGroupMapper.selectList(wrapper);
        List<DatasetTreeNodeVO> collect = coreDatasetTables.stream().map(ele -> {
            DatasetTreeNodeVO vo = new DatasetTreeNodeVO();
            BeanUtils.copyBean(vo, ele);
            vo.setLeaf(!StringUtils.equalsIgnoreCase(ele.getNodeType(), "folder"));
            return vo;
        }).collect(Collectors.toList());
        return TreeUtils.mergeTree(collect);
    }*/

    public void filterNode(List<BusiNodeVO> list) {
        if (ObjectUtils.isNotEmpty(list)) {
            list.removeIf(BusiNodeVO::getLeaf);
            for (BusiNodeVO dto : list) {
                if (ObjectUtils.isNotEmpty(dto.getChildren())) {
                    filterNode(dto.getChildren());
                }
            }
        }
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
            DEException.throwException(Translator.get("i18n_ds_name_exists"));
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

    public void saveField(DatasetGroupInfoDTO datasetGroupInfoDTO, List<Long> fieldIds) throws Exception {
        if (ObjectUtils.isEmpty(datasetGroupInfoDTO.getUnion())) {
            return;
        }
        datasetDataManage.previewDataWithLimit(datasetGroupInfoDTO, 0, 1);
        // table和field均由前端生成id（如果没有id）
        Long datasetGroupId = datasetGroupInfoDTO.getId();
        List<DatasetTableFieldDTO> allFields = datasetGroupInfoDTO.getAllFields();
        if (ObjectUtils.isNotEmpty(allFields)) {
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
    }

    public DatasetGroupInfoDTO get(Long id, String type) throws Exception {
        CoreDatasetGroup coreDatasetGroup = coreDatasetGroupMapper.selectById(id);
        if (coreDatasetGroup == null) {
            return null;
        }
        DatasetGroupInfoDTO dto = new DatasetGroupInfoDTO();
        BeanUtils.copyBean(dto, coreDatasetGroup);
        dto.setUnionSql(null);
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
                Map<String, Object> map = datasetDataManage.previewDataWithLimit(dto, 0, 100);
                // 获取data,sql
                Map<String, List> data = (Map<String, List>) map.get("data");
                String sql = (String) map.get("sql");
                Long total = (Long) map.get("total");
                dto.setData(data);
                dto.setSql(Base64.getEncoder().encodeToString(sql.getBytes()));
                dto.setTotal(total);
            }
        }
        return dto;
    }

    public List<DatasetTableDTO> getDetail(List<Long> ids) {
        if (ObjectUtils.isEmpty(ids)) {
            DEException.throwException(Translator.get("i18n_table_id_can_not_empty"));
        }
        List<DatasetTableDTO> list = new ArrayList<>();
        for (Long id : ids) {
            CoreDatasetGroup coreDatasetGroup = coreDatasetGroupMapper.selectById(id);
            if (coreDatasetGroup == null) {
                list.add(null);
            } else {
                DatasetTableDTO dto = new DatasetTableDTO();
                BeanUtils.copyBean(dto, coreDatasetGroup);
                Map<String, List<DatasetTableFieldDTO>> listByDQ = datasetTableFieldManage.listByDQ(id);
                dto.setFields(listByDQ);
                list.add(dto);
            }
        }
        return list;
    }
}
