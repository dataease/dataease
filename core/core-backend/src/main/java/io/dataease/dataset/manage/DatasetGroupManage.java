package io.dataease.dataset.manage;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.api.dataset.union.UnionDTO;
import io.dataease.api.dataset.vo.DataSetBarVO;
import io.dataease.api.permissions.user.api.UserApi;
import io.dataease.api.permissions.user.vo.UserFormVO;
import io.dataease.dataset.dao.auto.entity.CoreDatasetGroup;
import io.dataease.dataset.dao.auto.entity.CoreDatasetTable;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetGroupMapper;
import io.dataease.dataset.dao.ext.mapper.CoreDataSetExtMapper;
import io.dataease.dataset.dao.ext.po.DataSetNodePO;
import io.dataease.dataset.dto.DataSetNodeBO;
import io.dataease.dataset.utils.TableUtils;
import io.dataease.dto.dataset.DatasetTableFieldDTO;
import io.dataease.engine.constant.ExtFieldConstant;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import io.dataease.license.config.XpackInteract;
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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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
    @Resource
    private PermissionManage permissionManage;
    @Resource
    private CoreDataSetExtMapper coreDataSetExtMapper;
    @Autowired(required = false)
    private UserApi userApi;

    private static final String leafType = "dataset";

    private Lock lock = new ReentrantLock();


    public DatasetGroupInfoDTO save(DatasetGroupInfoDTO datasetGroupInfoDTO, boolean rename) throws Exception {
        lock.lock();
        try {
            boolean isCreate;
            if (ObjectUtils.isEmpty(datasetGroupInfoDTO.getPid()) && ObjectUtils.isNotEmpty(datasetGroupInfoDTO.getId())) {
                CoreDatasetGroup coreDatasetGroup = coreDatasetGroupMapper.selectById(datasetGroupInfoDTO.getId());
                datasetGroupInfoDTO.setPid(coreDatasetGroup.getPid());
            }
            if (StringUtils.equalsIgnoreCase(datasetGroupInfoDTO.getNodeType(), leafType)) {
                if (!rename && ObjectUtils.isEmpty(datasetGroupInfoDTO.getAllFields())) {
                    DEException.throwException(Translator.get("i18n_no_fields"));
                }
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
            if (ObjectUtils.isEmpty(datasetGroupInfoDTO.getId())) {
                isCreate = true;
                datasetGroupInfoDTO.setId(IDUtils.snowID());
                if (userApi != null) {
                    datasetGroupInfoDTO.setCreateBy(userApi.info().getId() + "");
                    datasetGroupInfoDTO.setUpdateBy(userApi.info().getId() + "");
                }
                datasetGroupInfoDTO.setCreateTime(time);
                datasetGroupInfoDTO.setLastUpdateTime(time);
                datasetGroupInfoDTO.setPid(datasetGroupInfoDTO.getPid() == null ? 0L : datasetGroupInfoDTO.getPid());
                Objects.requireNonNull(CommonBeanFactory.getBean(this.getClass())).innerSave(datasetGroupInfoDTO);
            } else {
                isCreate = false;
                if (Objects.equals(datasetGroupInfoDTO.getId(), datasetGroupInfoDTO.getPid())) {
                    DEException.throwException(Translator.get("i18n_pid_not_eq_id"));
                }
                if (userApi != null) {
                    datasetGroupInfoDTO.setUpdateBy(userApi.info().getId() + "");
                }
                Objects.requireNonNull(CommonBeanFactory.getBean(this.getClass())).innerEdit(datasetGroupInfoDTO);
            }
            // node_type=dataset需要创建dataset_table和field
            if (StringUtils.equalsIgnoreCase(datasetGroupInfoDTO.getNodeType(), "dataset")) {
                List<Long> tableIds = new ArrayList<>();
                List<Long> fieldIds = new ArrayList<>();
                // 解析tree，保存
                saveTable(datasetGroupInfoDTO, datasetGroupInfoDTO.getUnion(), tableIds, isCreate);
                saveField(datasetGroupInfoDTO, fieldIds);
                // 删除不要的table和field
                datasetTableManage.deleteByDatasetGroupUpdate(datasetGroupInfoDTO.getId(), tableIds);
                datasetTableFieldManage.deleteByDatasetGroupUpdate(datasetGroupInfoDTO.getId(), fieldIds);
            }
            return datasetGroupInfoDTO;
        } catch (Exception e) {
            DEException.throwException(e.getMessage());
        } finally {
            lock.unlock();
        }
        return null;
    }

    @XpackInteract(value = "authResourceTree", before = false)
    public void innerEdit(DatasetGroupInfoDTO datasetGroupInfoDTO) {
        CoreDatasetGroup coreDatasetGroup = BeanUtils.copyBean(new CoreDatasetGroup(), datasetGroupInfoDTO);
        coreDatasetGroup.setLastUpdateTime(System.currentTimeMillis());
        coreDatasetGroupMapper.updateById(coreDatasetGroup);
    }

    @XpackInteract(value = "authResourceTree", before = false)
    public void innerSave(DatasetGroupInfoDTO datasetGroupInfoDTO) {
        CoreDatasetGroup coreDatasetGroup = BeanUtils.copyBean(new CoreDatasetGroup(), datasetGroupInfoDTO);
        coreDatasetGroupMapper.insert(coreDatasetGroup);
    }

    @XpackInteract(value = "authResourceTree", before = false)
    public DatasetGroupInfoDTO move(DatasetGroupInfoDTO datasetGroupInfoDTO) {
        if (datasetGroupInfoDTO.getPid() != 0) {
            checkMove(datasetGroupInfoDTO);
        }
        // save dataset/group
        long time = System.currentTimeMillis();
        CoreDatasetGroup coreDatasetGroup = new CoreDatasetGroup();
        BeanUtils.copyBean(coreDatasetGroup, datasetGroupInfoDTO);
        if (userApi != null) {
            datasetGroupInfoDTO.setUpdateBy(userApi.info().getId() + "");
        }
        coreDatasetGroup.setLastUpdateTime(time);
        coreDatasetGroupMapper.updateById(coreDatasetGroup);
        return datasetGroupInfoDTO;
    }

    @XpackInteract(value = "authResourceTree", before = false)
    public void delete(Long id) {
        CoreDatasetGroup coreDatasetGroup = coreDatasetGroupMapper.selectById(id);
        if (ObjectUtils.isEmpty(coreDatasetGroup)) {
            DEException.throwException("resource not exist");
        }
        Objects.requireNonNull(CommonBeanFactory.getBean(this.getClass())).recursionDel(id);
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


    @XpackInteract(value = "authResourceTree", replace = true)
    public List<BusiNodeVO> tree(BusiNodeRequest request) {

        QueryWrapper<Object> queryWrapper = new QueryWrapper<>();
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
        DataSetBarVO dataSetBarVO = coreDataSetExtMapper.queryBarInfo(id);
        // get creator
        if (userApi != null) {
            UserFormVO userFormVO = userApi.queryById(Long.valueOf(dataSetBarVO.getCreateBy()));
            if (userFormVO != null) {
                dataSetBarVO.setCreator(userFormVO.getName());
            }
        }
        return dataSetBarVO;
    }

    private DataSetNodeBO rootNode() {
        return new DataSetNodeBO(0L, "root", false, 3, -1L, 0);
    }

    private DataSetNodeBO convert(DataSetNodePO po) {
        return new DataSetNodeBO(po.getId(), po.getName(), StringUtils.equals(po.getNodeType(), leafType), 3, po.getPid(), 0);
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

    public void saveTable(DatasetGroupInfoDTO datasetGroupInfoDTO, List<UnionDTO> union, List<Long> tableIds, boolean isCreate) {
        // table和field均由前端生成id（如果没有id）
        Long datasetGroupId = datasetGroupInfoDTO.getId();
        if (ObjectUtils.isNotEmpty(union)) {
            for (UnionDTO unionDTO : union) {
                DatasetTableDTO currentDs = unionDTO.getCurrentDs();
                CoreDatasetTable coreDatasetTable = datasetTableManage.selectById(currentDs.getId());
                if (coreDatasetTable != null && isCreate) {
                    DEException.throwException(Translator.get("i18n_table_duplicate"));
                }
                currentDs.setDatasetGroupId(datasetGroupId);
                datasetTableManage.save(currentDs);
                tableIds.add(currentDs.getId());

                saveTable(datasetGroupInfoDTO, unionDTO.getChildrenDs(), tableIds, isCreate);
            }
        }
    }

    public void saveField(DatasetGroupInfoDTO datasetGroupInfoDTO, List<Long> fieldIds) throws Exception {
        if (ObjectUtils.isEmpty(datasetGroupInfoDTO.getUnion())) {
            return;
        }
        datasetDataManage.previewDataWithLimit(datasetGroupInfoDTO, 0, 1, false);
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
        // get creator
        if (userApi != null) {
            UserFormVO userFormVO = userApi.queryById(Long.valueOf(dto.getCreateBy()));
            if (userFormVO != null) {
                dto.setCreator(userFormVO.getName());
            }
            UserFormVO userFormVOUpdateBy = userApi.queryById(Long.valueOf(dto.getUpdateBy()));
            if (userFormVOUpdateBy != null) {
                dto.setUpdater(userFormVOUpdateBy.getName());
            }
        }
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
                Map<String, Object> map = datasetDataManage.previewDataWithLimit(dto, 0, 100, true);
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

    public void checkMove(DatasetGroupInfoDTO datasetGroupInfoDTO) {
        if (Objects.equals(datasetGroupInfoDTO.getId(), datasetGroupInfoDTO.getPid())) {
            DEException.throwException(Translator.get("i18n_pid_not_eq_id"));
        }
        List<Long> ids = new ArrayList<>();
        getParents(datasetGroupInfoDTO.getPid(), ids);
        if (ids.contains(datasetGroupInfoDTO.getId())) {
            DEException.throwException(Translator.get("i18n_pid_not_eq_id"));
        }
    }

    private void getParents(Long pid, List<Long> ids) {
        CoreDatasetGroup parent = coreDatasetGroupMapper.selectById(pid);// 查找父级folder
        ids.add(parent.getId());
        if (parent.getPid() != null && parent.getPid() != 0) {
            getParents(parent.getPid(), ids);
        }
    }
}
