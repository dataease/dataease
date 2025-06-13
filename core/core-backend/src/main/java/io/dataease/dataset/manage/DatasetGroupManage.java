package io.dataease.dataset.manage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.api.dataset.union.UnionDTO;
import io.dataease.api.dataset.vo.DataSetBarVO;
import io.dataease.api.permissions.relation.api.RelationApi;
import io.dataease.commons.constants.OptConstants;
import io.dataease.dataset.dao.auto.entity.CoreDatasetGroup;
import io.dataease.dataset.dao.auto.entity.CoreDatasetTable;
import io.dataease.dataset.dao.auto.entity.QCoreDatasetGroup;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetGroupRepository;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetTableRepository;
import io.dataease.dataset.dao.ext.po.DataSetNodePO;
import io.dataease.dataset.dto.DataSetNodeBO;
import io.dataease.dataset.utils.DatasetUtils;
import io.dataease.dataset.utils.FieldUtils;
import io.dataease.dataset.utils.TableUtils;
import io.dataease.datasource.dao.auto.repository.CoreDatasourceRepository;
import io.dataease.engine.constant.ExtFieldConstant;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.DatasetTableDTO;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.datasource.dto.DatasourceDTO;
import io.dataease.extensions.view.dto.SqlVariableDetails;
import io.dataease.i18n.Translator;
import io.dataease.license.config.XpackInteract;
import io.dataease.license.utils.LicenseUtil;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import io.dataease.operation.manage.CoreOptRecentManage;
import io.dataease.system.manage.CoreUserManage;
import io.dataease.utils.*;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
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
    private CoreDatasetGroupRepository coreDatasetGroupRepository;
    @Resource
    private DatasetSQLManage datasetSQLManage;
    @Resource
    private DatasetDataManage datasetDataManage;
    @Resource
    private DatasetTableManage datasetTableManage;
    @Resource
    private DatasetTableFieldManage datasetTableFieldManage;
    @Resource
    private CoreDatasetTableRepository coreDatasetTableRepository;
    @Resource
    private CoreUserManage coreUserManage;
    @Resource
    private CoreOptRecentManage coreOptRecentManage;
    @Autowired(required = false)
    private RelationApi relationManage;
    @Autowired
    private CoreDatasourceRepository coreDatasourceRepository;

    private final JPAQueryFactory queryFactory;

    @Autowired
    public DatasetGroupManage(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    private static final String leafType = "dataset";

    private Lock lock = new ReentrantLock();


    @Transactional
    public DatasetGroupInfoDTO save(DatasetGroupInfoDTO datasetGroupInfoDTO, boolean rename, boolean encode) throws Exception {
        try {
            boolean isCreate;
            // 用于重命名获取pid
            if (ObjectUtils.isEmpty(datasetGroupInfoDTO.getPid()) && ObjectUtils.isNotEmpty(datasetGroupInfoDTO.getId())) {
                CoreDatasetGroup coreDatasetGroup = coreDatasetGroupRepository.findById(datasetGroupInfoDTO.getId()).orElse(null);
                datasetGroupInfoDTO.setPid(coreDatasetGroup.getPid());
            }
            datasetGroupInfoDTO.setUpdateBy(AuthUtils.getUser().getUserId() + "");
            datasetGroupInfoDTO.setLastUpdateTime(System.currentTimeMillis());
            if (StringUtils.equalsIgnoreCase(datasetGroupInfoDTO.getNodeType(), leafType)) {
                if (!rename && ObjectUtils.isEmpty(datasetGroupInfoDTO.getAllFields())) {
                    DEException.throwException(Translator.get("i18n_no_fields"));
                }
                // get union sql
                Map<String, Object> sqlMap = datasetSQLManage.getUnionSQLForEdit(datasetGroupInfoDTO, null);
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
                datasetGroupInfoDTO.setCreateBy(AuthUtils.getUser().getUserId() + "");
                datasetGroupInfoDTO.setUpdateBy(AuthUtils.getUser().getUserId() + "");
                datasetGroupInfoDTO.setCreateTime(time);
                datasetGroupInfoDTO.setLastUpdateTime(time);
                datasetGroupInfoDTO.setPid(datasetGroupInfoDTO.getPid() == null ? 0L : datasetGroupInfoDTO.getPid());
                Objects.requireNonNull(CommonBeanFactory.getBean(this.getClass())).innerSave(datasetGroupInfoDTO);
            } else {
                isCreate = false;
                if (Objects.equals(datasetGroupInfoDTO.getId(), datasetGroupInfoDTO.getPid())) {
                    DEException.throwException(Translator.get("i18n_pid_not_eq_id"));
                }
                Objects.requireNonNull(CommonBeanFactory.getBean(this.getClass())).innerEdit(datasetGroupInfoDTO);
            }
            // node_type=dataset需要创建dataset_table和field
            if (StringUtils.equalsIgnoreCase(datasetGroupInfoDTO.getNodeType(), "dataset")) {
                if (encode) {
                    DatasetUtils.dsDecode(datasetGroupInfoDTO);
                }
                List<Long> tableIds = new ArrayList<>();
                List<Long> fieldIds = new ArrayList<>();
                // 解析tree，保存
                saveTable(datasetGroupInfoDTO, datasetGroupInfoDTO.getUnion(), tableIds, isCreate);
                saveField(datasetGroupInfoDTO, fieldIds);
                // 删除不要的table和field
                datasetTableManage.deleteByDatasetGroupUpdate(datasetGroupInfoDTO.getId(), tableIds);
                datasetTableFieldManage.deleteByDatasetGroupUpdate(datasetGroupInfoDTO.getId(), fieldIds);
                if (encode) {
                    DatasetUtils.dsEncode(datasetGroupInfoDTO);
                }
            }
            if (StringUtils.isNotEmpty(datasetGroupInfoDTO.getUnionSql())) {
                datasetGroupInfoDTO.setUnionSql(DatasetUtils.getEncode(datasetGroupInfoDTO.getUnionSql()));
            }
            return datasetGroupInfoDTO;
        } catch (Exception e) {
            DEException.throwException(e.getMessage());
        }
        return null;
    }

    @XpackInteract(value = "authResourceTree", before = false)
    public void innerEdit(DatasetGroupInfoDTO datasetGroupInfoDTO) {
        checkName(datasetGroupInfoDTO);
        CoreDatasetGroup coreDatasetGroup = BeanUtils.copyBean(new CoreDatasetGroup(), datasetGroupInfoDTO);
        coreDatasetGroup.setLastUpdateTime(System.currentTimeMillis());
        coreDatasetGroupRepository.saveAndFlush(coreDatasetGroup);
        coreOptRecentManage.saveOpt(datasetGroupInfoDTO.getId(), OptConstants.OPT_RESOURCE_TYPE.DATASET, OptConstants.OPT_TYPE.UPDATE);
    }

    @XpackInteract(value = "authResourceTree", before = false)
    public void innerSave(DatasetGroupInfoDTO datasetGroupInfoDTO) {
        checkName(datasetGroupInfoDTO);
        CoreDatasetGroup coreDatasetGroup = BeanUtils.copyBean(new CoreDatasetGroup(), datasetGroupInfoDTO);
        coreDatasetGroupRepository.saveAndFlush(coreDatasetGroup);
        coreOptRecentManage.saveOpt(coreDatasetGroup.getId(), OptConstants.OPT_RESOURCE_TYPE.DATASET, OptConstants.OPT_TYPE.NEW);
    }

    @XpackInteract(value = "authResourceTree", before = false)
    public DatasetGroupInfoDTO move(DatasetGroupInfoDTO datasetGroupInfoDTO) {
        checkName(datasetGroupInfoDTO);
        if (datasetGroupInfoDTO.getPid() != 0) {
            checkMove(datasetGroupInfoDTO);
        }
        // save dataset/group
        long time = System.currentTimeMillis();
        CoreDatasetGroup coreDatasetGroup = new CoreDatasetGroup();
        BeanUtils.copyBean(coreDatasetGroup, datasetGroupInfoDTO);
        datasetGroupInfoDTO.setUpdateBy(AuthUtils.getUser().getUserId() + "");
        coreDatasetGroup.setLastUpdateTime(time);
        coreDatasetGroupRepository.saveAndFlush(coreDatasetGroup);
        coreOptRecentManage.saveOpt(coreDatasetGroup.getId(), OptConstants.OPT_RESOURCE_TYPE.DATASET, OptConstants.OPT_TYPE.UPDATE);
        return datasetGroupInfoDTO;
    }

    public boolean perDelete(Long id) {
        if (LicenseUtil.licenseValid()) {
            try {
                relationManage.checkAuth();
            } catch (Exception e) {
                return false;
            }
            Long count = relationManage.getDatasetResource(id);
            if (count > 0) {
                return true;
            }
        }
        return false;
    }

    @XpackInteract(value = "authResourceTree", before = false)
    public void delete(Long id) {
        CoreDatasetGroup coreDatasetGroup = coreDatasetGroupRepository.findById(id).orElse(null);
        if (ObjectUtils.isEmpty(coreDatasetGroup)) {
            DEException.throwException("resource not exist");
        }
        Objects.requireNonNull(CommonBeanFactory.getBean(this.getClass())).recursionDel(id);
        coreOptRecentManage.saveOpt(coreDatasetGroup.getId(), OptConstants.OPT_RESOURCE_TYPE.DATASET, OptConstants.OPT_TYPE.DELETE);
    }

    public void recursionDel(Long id) {
        coreDatasetGroupRepository.deleteById(id);
        datasetTableManage.deleteByDatasetGroupDelete(id);
        datasetTableFieldManage.deleteByDatasetGroupDelete(id);
        Specification<CoreDatasetGroup> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("pid"), id));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<CoreDatasetGroup> coreDatasetGroups = coreDatasetGroupRepository.findAll(spec);
        if (ObjectUtils.isNotEmpty(coreDatasetGroups)) {
            for (CoreDatasetGroup record : coreDatasetGroups) {
                recursionDel(record.getId());
            }
        }
    }


    @XpackInteract(value = "authResourceTree", replace = true, invalid = true)
    public List<BusiNodeVO> tree(BusiNodeRequest request) {
        String info = CommunityUtils.getInfo();
        if (StringUtils.isNotBlank(info)) {
            //TODO CommunityUtils.getInfo
//            queryWrapper.notExists(String.format(info, "core_dataset_group.id"));
        }
        QCoreDatasetGroup coreDatasetGroup = QCoreDatasetGroup.coreDatasetGroup;
        JPAQuery<DataSetNodePO> jpaQuery = queryFactory.select(
                        Projections.constructor(DataSetNodePO.class,
                                coreDatasetGroup.id,
                                coreDatasetGroup.name,
                                coreDatasetGroup.nodeType,
                                coreDatasetGroup.pid
                        )
                )
                .from(coreDatasetGroup);
        if (ObjectUtils.isNotEmpty(request.getLeaf()) && !request.getLeaf()) {
            jpaQuery.where(coreDatasetGroup.nodeType.eq(request.getLeaf() ? "dataset" : "folder"));
        }
        jpaQuery.orderBy(coreDatasetGroup.createTime.desc());

        List<DataSetNodePO> pos = jpaQuery.fetch();
        List<DataSetNodeBO> nodes = new ArrayList<>();
        if (ObjectUtils.isEmpty(request.getLeaf()) || !request.getLeaf()) nodes.add(rootNode());
        List<DataSetNodeBO> bos = pos.stream().map(this::convert).toList();
        if (CollectionUtils.isNotEmpty(bos)) {
            nodes.addAll(bos);
        }
        return TreeUtils.mergeTree(nodes, BusiNodeVO.class, false);
    }

    public DataSetBarVO queryBarInfo(Long id) {
        DataSetBarVO dataSetBarVO = new DataSetBarVO();
        CoreDatasetGroup coreDatasetGroup = coreDatasetGroupRepository.findById(id).orElse(null);
        if (ObjectUtils.isEmpty(coreDatasetGroup)) {
            BeanUtils.copyBean(dataSetBarVO, coreDatasetGroup);
        }
        // get creator
        String userName = coreUserManage.getUserName(Long.valueOf(dataSetBarVO.getCreateBy()));
        if (StringUtils.isNotBlank(userName)) {
            dataSetBarVO.setCreator(userName);
        }
        String updateUserName = coreUserManage.getUserName(Long.valueOf(dataSetBarVO.getUpdateBy()));
        if (StringUtils.isNotBlank(updateUserName)) {
            dataSetBarVO.setUpdater(updateUserName);
        }
        dataSetBarVO.setDatasourceDTOList(getDatasource(id));
        return dataSetBarVO;
    }

    private List<DatasourceDTO> getDatasource(Long datasetId) {
        List<CoreDatasetTable> coreDatasetTables = coreDatasetTableRepository.findByDatasetGroupId(datasetId);
        List<Long> ids = new ArrayList<>();
        coreDatasetTables.forEach(ele -> ids.add(ele.getDatasourceId()));
        if (CollectionUtils.isEmpty(ids)) {
            DEException.throwException(Translator.get("i18n_dataset_create_error"));
        }
        List<DatasourceDTO> datasourceDTOList = coreDatasourceRepository.findInIds(ids).stream().map(ele -> {
            DatasourceDTO dto = new DatasourceDTO();
            BeanUtils.copyBean(dto, ele);
            dto.setConfiguration(null);
            return dto;
        }).collect(Collectors.toList());
        if (ids.size() != datasourceDTOList.size()) {
            DEException.throwException(Translator.get("i18n_dataset_ds_delete"));
        }
        return datasourceDTOList;
    }

    private DataSetNodeBO rootNode() {
        return new DataSetNodeBO(0L, "root", false, 7, -1L, 0);
    }

    private DataSetNodeBO convert(DataSetNodePO po) {
        return new DataSetNodeBO(po.getId(), po.getName(), StringUtils.equals(po.getNodeType(), leafType), 9, po.getPid(), 0);
    }

    public void checkName(DatasetGroupInfoDTO dto) {
        if (!LicenseUtil.licenseValid()) {
            Specification<CoreDatasetGroup> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                if (dto.getPid() != null) {
                    predicates.add(cb.equal(root.get("pid"), dto.getPid()));
                }
                if (dto.getName() != null && !dto.getName().isEmpty()) {
                    predicates.add(cb.equal(root.get("name"), dto.getName()));
                }
                if (dto.getId() != null) {
                    predicates.add(cb.notEqual(root.get("id"), dto.getId()));
                }
                if (dto.getLevel() != null) {
                    predicates.add(cb.equal(root.get("level"), dto.getLevel()));
                }
                if (dto.getNodeType() != null) {
                    predicates.add(cb.equal(root.get("nodeType"), dto.getNodeType()));
                }
                return cb.and(predicates.toArray(new Predicate[0]));
            };
            List<CoreDatasetGroup> list = coreDatasetGroupRepository.findAll(spec);
            if (CollectionUtils.isNotEmpty(list)) {
                DEException.throwException(Translator.get("i18n_ds_name_exists"));
            }
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
        datasetDataManage.previewDataWithLimit(datasetGroupInfoDTO, 0, 1, false, false);
        // table和field均由前端生成id（如果没有id）
        Long datasetGroupId = datasetGroupInfoDTO.getId();
        List<DatasetTableFieldDTO> allFields = datasetGroupInfoDTO.getAllFields();
        if (ObjectUtils.isNotEmpty(allFields)) {
            // 获取内层union sql和字段
            Map<String, Object> map = datasetSQLManage.getUnionSQLForEdit(datasetGroupInfoDTO, null);
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
                    if (Objects.equals(datasetTableFieldDTO.getExtField(), ExtFieldConstant.EXT_GROUP)) {
                        String dataeaseName = TableUtils.fieldNameShort(datasetTableFieldDTO.getId() + "_" + datasetTableFieldDTO.getOriginName());
                        datasetTableFieldDTO.setDataeaseName(dataeaseName);
                        datasetTableFieldDTO.setFieldShortName(dataeaseName);
                        datasetTableFieldDTO.setDeExtractType(0);
                        datasetTableFieldDTO.setDeType(0);
                        datasetTableFieldDTO.setGroupType("d");
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
            List<DatasetTableFieldDTO> dsFields = datasetTableFieldManage.selectByDatasetGroupId(id);
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

    public DatasetGroupInfoDTO getDetail(Long id) throws Exception {
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
            List<DatasetTableFieldDTO> dsFields = datasetTableFieldManage.selectByDatasetGroupId(id);
            List<DatasetTableFieldDTO> allFields = dsFields.stream().map(ele -> {
                DatasetTableFieldDTO datasetTableFieldDTO = new DatasetTableFieldDTO();
                BeanUtils.copyBean(datasetTableFieldDTO, ele);
                datasetTableFieldDTO.setFieldShortName(ele.getDataeaseName());
                return datasetTableFieldDTO;
            }).collect(Collectors.toList());

            DatasetUtils.listEncode(allFields);

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
                Map<String, Object> map = datasetDataManage.previewDataWithLimit(dto, 0, 100, true, false);
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
            CoreDatasetGroup coreDatasetGroup = coreDatasetGroupRepository.findById(id).orElse(null);
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

    public List<SqlVariableDetails> getSqlParams(List<Long> ids) {
        List<SqlVariableDetails> list = new ArrayList<>();
        if (ObjectUtils.isEmpty(ids)) {
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
        CoreDatasetGroup parent = coreDatasetGroupRepository.findById(pid).orElse(null);// 查找父级folder
        ids.add(parent.getId());
        if (parent.getPid() != null && parent.getPid() != 0) {
            getParents(parent.getPid(), ids);
        }
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

    public List<DatasetTableDTO> getDetailWithPerm(List<Long> ids) {
        var result = new ArrayList<DatasetTableDTO>();
        if (CollectionUtils.isNotEmpty(ids)) {
            var dsList = coreDatasetGroupRepository.findAllById(ids);
            if (CollectionUtils.isNotEmpty(dsList)) {
                dsList.forEach(ds -> {
                    DatasetTableDTO dto = new DatasetTableDTO();
                    BeanUtils.copyBean(dto, ds);
                    var fields = datasetTableFieldManage.listFieldsWithPermissions(ds.getId());
                    List<DatasetTableFieldDTO> dimensionList = fields.stream().filter(ele -> StringUtils.equalsIgnoreCase(ele.getGroupType(), "d")).toList();
                    List<DatasetTableFieldDTO> quotaList = fields.stream().filter(ele -> StringUtils.equalsIgnoreCase(ele.getGroupType(), "q")).toList();
                    Map<String, List<DatasetTableFieldDTO>> map = new LinkedHashMap<>();
                    DatasetUtils.listEncode(dimensionList);
                    DatasetUtils.listEncode(quotaList);
                    map.put("dimensionList", dimensionList);
                    map.put("quotaList", quotaList);
                    dto.setFields(map);
                    result.add(dto);
                });
            }
        }
        return result;
    }

    public List<DatasetGroupInfoDTO> getAllList() {
        List<CoreDatasetGroup> coreDatasetGroupList = coreDatasetGroupRepository.findAll();
        if (CollectionUtils.isEmpty(coreDatasetGroupList)) {
            return new ArrayList<>();
        }
        List<DatasetGroupInfoDTO> list = new ArrayList<>();
        for (CoreDatasetGroup coreDatasetGroup : coreDatasetGroupList) {
            DatasetGroupInfoDTO dto = new DatasetGroupInfoDTO();
            BeanUtils.copyBean(dto, coreDatasetGroup);
            dto.setUnionSql(null);
            if (StringUtils.equalsIgnoreCase(dto.getNodeType(), "dataset")) {
                List<UnionDTO> unionDTOList = JsonUtil.parseList(coreDatasetGroup.getInfo(), new TypeReference<>() {
                });
                dto.setUnion(unionDTOList);

                // 获取field
                List<DatasetTableFieldDTO> dsFields = datasetTableFieldManage.selectByDatasetGroupId(coreDatasetGroup.getId());
                List<DatasetTableFieldDTO> allFields = dsFields.stream().map(ele -> {
                    DatasetTableFieldDTO datasetTableFieldDTO = new DatasetTableFieldDTO();
                    BeanUtils.copyBean(datasetTableFieldDTO, ele);
                    datasetTableFieldDTO.setFieldShortName(ele.getDataeaseName());
                    return datasetTableFieldDTO;
                }).collect(Collectors.toList());

                DatasetUtils.listEncode(allFields);

                dto.setAllFields(allFields);

                list.add(dto);
            }
        }
        return list;
    }
}
