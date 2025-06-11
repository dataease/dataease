package io.dataease.datasource.manage;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.dataease.commons.constants.OptConstants;
import io.dataease.commons.constants.TaskStatus;
import io.dataease.commons.utils.EncryptUtils;
import io.dataease.datasource.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.dao.auto.entity.QCoreDatasource;
import io.dataease.datasource.dao.auto.repository.CoreDatasourceRepository;
import io.dataease.datasource.dao.ext.po.DataSourceNodePO;
import io.dataease.datasource.dto.DatasourceNodeBO;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.api.PluginManageApi;
import io.dataease.extensions.datasource.dto.DatasourceDTO;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import io.dataease.extensions.datasource.vo.XpackPluginsDatasourceVO;
import io.dataease.i18n.Translator;
import io.dataease.license.config.XpackInteract;
import io.dataease.license.utils.LicenseUtil;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import io.dataease.operation.manage.CoreOptRecentManage;
import io.dataease.utils.*;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

@Component
public class DataSourceManage {

    private final JPAQueryFactory queryFactory;

    @Autowired
    public DataSourceManage(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Autowired
    private CoreDatasourceRepository coreDatasourceRepository;
    @Resource
    private CoreOptRecentManage coreOptRecentManage;

    @Resource
    private EngineManage engineManage;

    @Autowired(required = false)
    private PluginManageApi pluginManage;

    private DatasourceNodeBO rootNode() {
        return new DatasourceNodeBO(0L, "root", false, 7, -1L, 0, "mysql");
    }

    private Integer getFlag(String type) {
        Integer flag = null;
        for (DatasourceConfiguration.DatasourceType datasourceType : DatasourceConfiguration.DatasourceType.values()) {
            if (datasourceType.getType().equals(type)) {
                flag = datasourceType.getFlag();
            }
        }
        if (ObjectUtils.isEmpty(flag)) {
            List<XpackPluginsDatasourceVO> xpackPluginsDatasourceVOS = pluginManage.queryPluginDs();
            List<XpackPluginsDatasourceVO> list = xpackPluginsDatasourceVOS.stream().filter(ele -> StringUtils.equals(ele.getType(), type)).toList();
            if (ObjectUtils.isNotEmpty(list)) {
                XpackPluginsDatasourceVO first = list.getFirst();
                flag = first.getFlag();
            }
        }
        if (ObjectUtils.isEmpty(flag)) {
            flag = 27;
        }
        return flag;
    }

    private DatasourceNodeBO convert(DataSourceNodePO po) {
        Integer flag = getFlag(po.getType());
        int extraFlag = StringUtils.equalsIgnoreCase("error", po.getStatus()) ? Math.negateExact(flag) : flag;
        return new DatasourceNodeBO(po.getId(), po.getName(), !StringUtils.equals(po.getType(), "folder"), 9, po.getPid(), extraFlag, po.getType());
    }

    @XpackInteract(value = "datasourceResourceTree", replace = true, invalid = true)
    public List<BusiNodeVO> tree(BusiNodeRequest request) {
        QCoreDatasource coreDatasource = QCoreDatasource.coreDatasource;
        JPAQuery<DataSourceNodePO> jpaQuery = queryFactory.select(
                        Projections.constructor(DataSourceNodePO.class,
                                coreDatasource.id,
                                coreDatasource.pid,
                                coreDatasource.name,
                                coreDatasource.type,
                                coreDatasource.status,
                                coreDatasource.createTime
                        )
                )
                .from(coreDatasource);
        if (ObjectUtils.isNotEmpty(request.getLeaf()) && !request.getLeaf()) {
            jpaQuery.where(coreDatasource.type.eq("folder"));
        }

        String info = CommunityUtils.getInfo();
        if (StringUtils.isNotBlank(info)) {
            //TODO
//            queryWrapper.notExists(String.format(info, "core_datasource.id"));
        }

        List<DatasourceNodeBO> nodes = new ArrayList<>();
        List<DataSourceNodePO> pos = jpaQuery.fetch();
        if (ObjectUtils.isEmpty(request.getLeaf()) || !request.getLeaf()) nodes.add(rootNode());
        if (CollectionUtils.isNotEmpty(pos)) {
            nodes.addAll(pos.stream().map(this::convert).toList());
        }
        return TreeUtils.mergeTree(nodes, BusiNodeVO.class, false);
    }


    @XpackInteract(value = "datasourceResourceTree", before = false)
    public void innerSave(DatasourceDTO dataSourceDTO) {
        CoreDatasource coreDatasource = new CoreDatasource();
        coreDatasource.setTaskStatus(TaskStatus.WaitingForExecution.name());
        BeanUtils.copyBean(coreDatasource, dataSourceDTO);
        checkName(dataSourceDTO);
        coreDatasourceRepository.saveAndFlush(coreDatasource);
        coreOptRecentManage.saveOpt(coreDatasource.getId(), OptConstants.OPT_RESOURCE_TYPE.DATASOURCE, OptConstants.OPT_TYPE.NEW);
    }

    public void checkName(DatasourceDTO dto) {
        if (StringUtils.isEmpty(dto.getName()) || StringUtils.isEmpty(dto.getName().trim())) {
            DEException.throwException(Translator.get("i18n_df_name_can_not_empty"));
        }
        Specification<CoreDatasource> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (ObjectUtils.isNotEmpty(dto.getPid())) {
                if (LicenseUtil.licenseValid() && dto.getPid().equals(0L)) {
                    predicates.add(criteriaBuilder.equal(root.get("pid"), -100L));
                } else {
                    predicates.add(criteriaBuilder.equal(root.get("pid"), dto.getPid()));
                }
            }
            if (StringUtils.isNotEmpty(dto.getName())) {
                predicates.add(criteriaBuilder.equal(root.get("name"), dto.getName()));
            }
            if (ObjectUtils.isNotEmpty(dto.getId())) {
                predicates.add(criteriaBuilder.notEqual(root.get("id"), dto.getId()));
            }
            if (ObjectUtils.isNotEmpty(dto.getNodeType())) {
                if (dto.getNodeType().equalsIgnoreCase("folder")) {
                    predicates.add(criteriaBuilder.equal(root.get("type"), dto.getType()));
                } else {
                    predicates.add(criteriaBuilder.notEqual(root.get("type"), "folder"));
                }
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        List<CoreDatasource> list = coreDatasourceRepository.findAll(spec);
        if (list.size() > 0) {
            DEException.throwException(Translator.get("i18n_ds_name_exists"));
        }
    }

    @XpackInteract(value = "larkManage", replace = true)
    public String getTenantAccessToken() {
        return null;
    }


    @XpackInteract(value = "datasourceResourceTree", before = false)
    public void innerEdit(CoreDatasource coreDatasource) {
        coreDatasource.setUpdateTime(System.currentTimeMillis());
        coreDatasource.setUpdateBy(AuthUtils.getUser().getUserId());
        coreDatasource.setTaskStatus(TaskStatus.WaitingForExecution.name());
        coreDatasourceRepository.save(coreDatasource);
        coreOptRecentManage.saveOpt(coreDatasource.getId(), OptConstants.OPT_RESOURCE_TYPE.DATASOURCE, OptConstants.OPT_TYPE.UPDATE);
    }


    @XpackInteract(value = "datasourceResourceTree", before = false)
    public void innerEditName(CoreDatasource coreDatasource) {
        Optional<CoreDatasource> optionalCoreDatasource = coreDatasourceRepository.findById(coreDatasource.getId());
        if (optionalCoreDatasource.isPresent()) {
            coreDatasourceRepository.move(coreDatasource.getId(), System.currentTimeMillis(), coreDatasource.getPid(), coreDatasource.getName(), AuthUtils.getUser().getUserId());
        }
        coreOptRecentManage.saveOpt(coreDatasource.getId(), OptConstants.OPT_RESOURCE_TYPE.DATASOURCE, OptConstants.OPT_TYPE.UPDATE);
    }

    @XpackInteract(value = "datasourceResourceTree", before = false)
    public void innerEditStatus(CoreDatasource coreDatasource) {
        coreDatasourceRepository.updateStatusById(coreDatasource.getStatus(), coreDatasource.getId());
    }


    @XpackInteract(value = "datasourceResourceTree", before = false)
    public void move(DatasourceDTO dataSourceDTO) {
        Long id = dataSourceDTO.getId();
        CoreDatasource sourceData = null;
        if (ObjectUtils.isEmpty(id) || ObjectUtils.isEmpty(sourceData = getCoreDatasource(id))) {
            DEException.throwException("resource not exist");
        }
        checkName(dataSourceDTO);
        coreDatasourceRepository.move(id, System.currentTimeMillis(), dataSourceDTO.getPid(), dataSourceDTO.getName(), AuthUtils.getUser().getUserId());
        coreOptRecentManage.saveOpt(sourceData.getId(), OptConstants.OPT_RESOURCE_TYPE.DATASOURCE, OptConstants.OPT_TYPE.UPDATE);
    }

    @XpackInteract(value = "datasourceResourceTree", before = false)
    public CoreDatasource getCoreDatasource(Long id) {
        CoreDatasource coreDatasource = new CoreDatasource();
        if (id == -1L) {
            coreDatasource = engineManage.getDeEngine();
        } else {
            coreDatasource = coreDatasourceRepository.findById(id).get();
        }
        coreDatasource.setConfiguration((String) EncryptUtils.aesDecrypt(coreDatasource.getConfiguration()));
        return coreDatasource;

    }

    public List<Long> getPidList(Long pid) {
        if (ObjectUtils.isEmpty(pid) || pid.equals(0L)) {
            return null;
        }
        List<Long> result = new ArrayList<>();
        Stack<Long> stack = new Stack<>();
        stack.push(pid);
        while (!stack.isEmpty()) {
            Long cid = stack.pop();
            coreDatasourceRepository.findById(cid).ifPresent(coreDatasource -> {
                result.add(cid);
                Long cpid = null;
                if (ObjectUtils.isNotEmpty(cpid = coreDatasource.getPid()) && !cpid.equals(0L)) {
                    stack.add(cpid);
                }
            });
        }
        return result;
    }

    public CoreDatasource getDatasource(Long id) {
        return getCoreDatasource(id);
    }

    public DatasourceDTO getDs(Long id) {
        CoreDatasource coreDatasource = getCoreDatasource(id);
        DatasourceDTO dto = new DatasourceDTO();
        BeanUtils.copyBean(dto, coreDatasource);
        return dto;
    }
}
