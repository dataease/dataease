package io.dataease.visualization.manage;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.dataease.api.visualization.request.VisualizationStoreRequest;
import io.dataease.api.visualization.request.VisualizationWorkbranchQueryRequest;
import io.dataease.api.visualization.vo.VisualizationStoreVO;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.dao.auto.entity.QDataVisualizationInfo;
import io.dataease.exception.DEException;
import io.dataease.license.config.XpackInteract;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.utils.IDUtils;
import io.dataease.visualization.dao.auto.entity.CoreStore;
import io.dataease.visualization.dao.auto.entity.QCoreStore;
import io.dataease.visualization.dao.auto.mapper.CoreStoreRepository;
import io.dataease.visualization.dao.ext.po.StorePO;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class VisualizationStoreManage {
    @Resource
    private JPAQueryFactory queryFactory;
    @Resource
    private CoreStoreRepository coreStoreRepository;


    public void execute(VisualizationStoreRequest request) {
        Long resourceId = request.getId();
        Long uid = AuthUtils.getUser().getUserId();
        if (favorited(resourceId)) {
            Specification<CoreStore> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("uid"), uid));
                predicates.add(cb.equal(root.get("resourceId"), resourceId));
                return cb.and(predicates.toArray(new Predicate[0]));
            };
            coreStoreRepository.delete(spec);
            return;
        }
        String type = request.getType();
        BusiResourceEnum busiResourceEnum = BusiResourceEnum.valueOf(type.toUpperCase());
        if (ObjectUtils.isEmpty(busiResourceEnum)) {
            DEException.throwException("type is invalid");
        }
        CoreStore coreStore = new CoreStore();
        coreStore.setId(IDUtils.snowID());
        coreStore.setTime(System.currentTimeMillis());
        coreStore.setUid(uid);
        coreStore.setResourceId(resourceId);
        coreStore.setResourceType(busiResourceEnum.getFlag());
        coreStoreRepository.saveAndFlush(coreStore);
    }

    public Boolean favorited(Long resourceId) {
        Specification<CoreStore> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("uid"), AuthUtils.getUser().getUserId()));
            predicates.add(cb.equal(root.get("resourceId"), resourceId));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return coreStoreRepository.exists(spec);
    }

    @XpackInteract(value = "perFilterManage", recursion = true, invalid = true)
    public Page<VisualizationStoreVO> query(int pageNum, int pageSize, VisualizationWorkbranchQueryRequest request) {
        Page<StorePO> storePOIPage = proxy().queryStorePage(pageNum, pageSize, request);
        if (ObjectUtils.isEmpty(storePOIPage)) return null;
        List<VisualizationStoreVO> vos = proxy().formatResult(storePOIPage.getContent());

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        return new PageImpl<>(vos, pageable, storePOIPage.getTotalElements());
    }

    public VisualizationStoreManage proxy() {
        return CommonBeanFactory.getBean(this.getClass());
    }

    public List<VisualizationStoreVO> formatResult(List<StorePO> pos) {
        if (CollectionUtils.isEmpty(pos)) return new ArrayList<>();
        return pos.stream().map(po ->
                new VisualizationStoreVO(
                        po.getStoreId(), po.getResourceId(), po.getName(),
                        po.getType(), String.valueOf(po.getCreator()), ObjectUtils.isEmpty(po.getEditor()) ? null : String.valueOf(po.getEditor()),
                        po.getEditTime(), 9, po.getExtFlag(), po.getExtFlag1())).toList();
    }

    public Page<StorePO> queryStorePage(int goPage, int pageSize, VisualizationWorkbranchQueryRequest request) {
        Long uid = AuthUtils.getUser().getUserId();

        QCoreStore coreStore = QCoreStore.coreStore;
        QDataVisualizationInfo dataVisualizationInfo = QDataVisualizationInfo.dataVisualizationInfo;
        JPAQuery<StorePO> query = queryFactory.select(Projections.fields(StorePO.class,
                        coreStore.id.as("storeId"),
                        dataVisualizationInfo.id.as("resourceId"),
                        dataVisualizationInfo.type,
                        dataVisualizationInfo.createBy.as("creator"),
                        dataVisualizationInfo.updateBy.as("editor"),
                        dataVisualizationInfo.updateTime.as("editTime"),
                        dataVisualizationInfo.name,
                        dataVisualizationInfo.mobileLayout.as("extFlag")))
                .from(coreStore)
                .innerJoin(dataVisualizationInfo).on(coreStore.resourceId.eq(Long.valueOf(dataVisualizationInfo.id.toString())))
                .where(coreStore.uid.eq(uid))
                .where(coreStore.resourceId.isNotNull())
                .orderBy(request.isAsc() ? dataVisualizationInfo.updateTime.asc() : dataVisualizationInfo.updateTime.desc());


        if (StringUtils.isNotBlank(request.getType())) {
            BusiResourceEnum busiResourceEnum = BusiResourceEnum.valueOf(request.getType().toUpperCase());
            if (ObjectUtils.isEmpty(busiResourceEnum)) {
                DEException.throwException("type is invalid");
            }
            query.where(coreStore.resourceType.eq(busiResourceEnum.getFlag()));
        }
        if (StringUtils.isNotBlank(request.getKeyword())) {
            query.where(dataVisualizationInfo.name.lower().likeIgnoreCase("%" + request.getKeyword().toUpperCase() + "%"));
        }

        //TODO CommunityUtils.getInfo
//        String info = CommunityUtils.getInfo();
//        if (StringUtils.isNotBlank(info)) {
//            queryWrapper.notExists(String.format(info, "s.resource_id"));
//        }

        Pageable pageable = PageRequest.of(goPage - 1, pageSize);
        long total = query.fetchCount();
        return new PageImpl<>(query.offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch(), pageable, total);

    }
}
