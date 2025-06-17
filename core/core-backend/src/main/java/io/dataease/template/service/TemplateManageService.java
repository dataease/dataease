package io.dataease.template.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.dataease.api.template.TemplateManageApi;
import io.dataease.api.template.dto.TemplateManageDTO;
import io.dataease.api.template.request.TemplateManageBatchRequest;
import io.dataease.api.template.request.TemplateManageRequest;
import io.dataease.api.template.vo.VisualizationTemplateVO;
import io.dataease.constant.CommonConstants;
import io.dataease.dao.auto.entity.CoreDatasetTableField;
import io.dataease.exception.DEException;
import io.dataease.template.dao.auto.entity.*;
import io.dataease.template.dao.auto.mapper.VisualizationTemplateCategoryMapRepository;
import io.dataease.template.dao.auto.mapper.VisualizationTemplateCategoryRepository;
import io.dataease.template.dao.auto.mapper.VisualizationTemplateRepository;
import io.dataease.template.dao.ext.ExtVisualizationTemplateMapper;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.BeanUtils;
import io.dataease.visualization.server.StaticResourceServer;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import static io.dataease.constant.StaticResourceConstants.UPLOAD_URL_PREFIX;

/**
 * @author : WangJiaHao
 * @date : 2023/11/7 13:29
 */
@RestController
@RequestMapping("/templateManage")
public class TemplateManageService implements TemplateManageApi {

    @Resource
    private VisualizationTemplateRepository visualizationTemplateRepository;
    @Resource
    private JPAQueryFactory queryFactory;
    @Resource
    private VisualizationTemplateCategoryRepository visualizationTemplateCategoryRepository;
    @Resource
    private VisualizationTemplateCategoryMapRepository visualizationTemplateCategoryMapRepository;
    @Resource
    private ExtVisualizationTemplateMapper extTemplateMapper;
    @Resource
    private StaticResourceServer staticResourceServer;

    @Override
    public List<TemplateManageDTO> templateList(TemplateManageRequest request) {
        request.setWithBlobs("N");
        List<TemplateManageDTO> templateList = extTemplateMapper.findTemplateList(request);
        if (request.getWithChildren()) {
            getTreeChildren(templateList, request.getLeafDvType());
        }
        return templateList;
    }

    public void getTreeChildren(List<TemplateManageDTO> parentTemplateList, String dvType) {
        Optional.ofNullable(parentTemplateList).ifPresent(parent -> parent.forEach(parentTemplate -> {
            List<TemplateManageDTO> panelTemplateDTOChildren = extTemplateMapper.findTemplateList(new TemplateManageRequest(parentTemplate.getId(), dvType));
            parentTemplate.setChildren(panelTemplateDTOChildren);
            getTreeChildren(panelTemplateDTOChildren, dvType);
        }));
    }

    public List<TemplateManageDTO> getSystemTemplateType(TemplateManageRequest request) {
        return extTemplateMapper.findTemplateList(request);
    }


    @Transactional
    @Override
    public TemplateManageDTO save(TemplateManageRequest request) {
        if (StringUtils.isEmpty(request.getId())) {
            request.setId(UUID.randomUUID().toString());
            request.setCreateTime(System.currentTimeMillis());
            request.setCreateBy(AuthUtils.getUser().getUserId().toString());
            if ("template".equals(request.getNodeType()) || "app".equals(request.getNodeType())) {
                //Store static resource into the server
                staticResourceServer.saveFilesToServe(request.getStaticResource());
                String snapshotName = request.getNodeType() + "-" + request.getId() + ".jpeg";
                staticResourceServer.saveSingleFileToServe(snapshotName, request.getSnapshot().replace("data:image/jpeg;base64,", ""));
                request.setSnapshot("/" + UPLOAD_URL_PREFIX + '/' + snapshotName);
            }
            //如果level 是0（第一级）指的是分类目录 设置父级为对应的templateType
            if (request.getLevel() == 0) {
                request.setPid(request.getTemplateType());
                String nameCheckResult = this.categoryNameCheck(CommonConstants.OPT_TYPE.INSERT, request.getName(), null);
                if (CommonConstants.CHECK_RESULT.EXIST_ALL.equals(nameCheckResult)) {
                    DEException.throwException("名称已存在");
                }
                VisualizationTemplateCategory templateCategory = new VisualizationTemplateCategory();
                BeanUtils.copyBean(templateCategory, request);
                visualizationTemplateCategoryRepository.saveAndFlush(templateCategory);
            } else {//模板插入 同名的模板进行覆盖(先删除)
                // 分类映射删除
                List<VisualizationTemplate> visualizationTemplates = visualizationTemplateRepository.findByName(request.getName());
                if (!CollectionUtils.isEmpty(visualizationTemplates)) {
                    visualizationTemplateCategoryMapRepository.deleteByTemplateIds(visualizationTemplates.stream().map(VisualizationTemplate::getId).toList());
                }


                // 模板删除
                visualizationTemplateRepository.deleteByTemplateName(request.getName());

                VisualizationTemplate template = new VisualizationTemplate();
                BeanUtils.copyBean(template, request);
                if (template.getVersion() == null) {
                    template.setVersion(2);
                }
                visualizationTemplateRepository.saveAndFlush(template);
                // 插入分类关系
                request.getCategories().forEach(categoryId -> {
                    VisualizationTemplateCategoryMap categoryMap = new VisualizationTemplateCategoryMap();
                    categoryMap.setId(UUID.randomUUID().toString());
                    categoryMap.setCategoryId(categoryId);
                    categoryMap.setTemplateId(template.getId());
                    visualizationTemplateCategoryMapRepository.saveAndFlush(categoryMap);
                });

            }
        } else {
            if (request.getLevel() == 0) {
                String nameCheckResult = this.categoryNameCheck(CommonConstants.OPT_TYPE.UPDATE, request.getName(), request.getId());
                if (CommonConstants.CHECK_RESULT.EXIST_ALL.equals(nameCheckResult)) {
                    DEException.throwException("名称已存在");
                }
                VisualizationTemplateCategory templateCategory = new VisualizationTemplateCategory();
                BeanUtils.copyBean(templateCategory, request);
                visualizationTemplateCategoryRepository.saveAndFlush(templateCategory);
            } else {
                String nameCheckResult = this.nameCheck(CommonConstants.OPT_TYPE.UPDATE, request.getName(), request.getId());
                if (CommonConstants.CHECK_RESULT.EXIST_ALL.equals(nameCheckResult)) {
                    DEException.throwException("名称已存在");
                }
                VisualizationTemplate template = new VisualizationTemplate();
                BeanUtils.copyBean(template, request);
                if (template.getVersion() == null) {
                    template.setVersion(2);
                }
                visualizationTemplateRepository.saveAndFlush(template);
                //更新分类
                // 分类映射删除
                visualizationTemplateCategoryMapRepository.deleteByTemplateId(request.getId());
                // 插入分类关系
                request.getCategories().forEach(categoryId -> {
                    VisualizationTemplateCategoryMap categoryMap = new VisualizationTemplateCategoryMap();
                    categoryMap.setId(UUID.randomUUID().toString());
                    categoryMap.setCategoryId(categoryId);
                    categoryMap.setTemplateId(request.getId());
                    visualizationTemplateCategoryMapRepository.saveAndFlush(categoryMap);
                });
            }

        }
        TemplateManageDTO templateManageDTO = new TemplateManageDTO();
        BeanUtils.copyBean(templateManageDTO, request);
        templateManageDTO.setLabel(request.getName());
        return templateManageDTO;
    }

    //模板名称检查
    public String nameCheck(String optType, String name, String id) {
        Specification<VisualizationTemplate> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (CommonConstants.OPT_TYPE.INSERT.equals(optType)) {
                predicates.add(cb.equal(root.get("name"), name));
            } else if (CommonConstants.OPT_TYPE.UPDATE.equals(optType)) {
                predicates.add(cb.equal(root.get("name"), name));
                predicates.add(cb.notEqual(root.get("id"), id));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<VisualizationTemplate> templateList = visualizationTemplateRepository.findAll(spec);
        if (CollectionUtils.isEmpty(templateList)) {
            return CommonConstants.CHECK_RESULT.NONE;
        } else {
            return CommonConstants.CHECK_RESULT.EXIST_ALL;
        }
    }

    //分类下模板名称检查
    @Override
    public String categoryTemplateNameCheck(TemplateManageRequest request) {
        QVisualizationTemplate qVisualizationTemplate = QVisualizationTemplate.visualizationTemplate;
        QVisualizationTemplateCategoryMap qVisualizationTemplateCategoryMap = QVisualizationTemplateCategoryMap.visualizationTemplateCategoryMap;
        Long result = queryFactory.selectFrom(qVisualizationTemplate)
                .leftJoin(qVisualizationTemplateCategoryMap).on(qVisualizationTemplate.id.eq(qVisualizationTemplateCategoryMap.templateId))
                .where(qVisualizationTemplate.name.eq(request.getName())
                        .and(qVisualizationTemplateCategoryMap.categoryId.in(request.getCategories()))).fetchCount();

        if (result == 0) {
            return CommonConstants.CHECK_RESULT.NONE;
        } else {
            return CommonConstants.CHECK_RESULT.EXIST_ALL;
        }
    }

    @Override
    public String checkCategoryTemplateBatchNames(TemplateManageRequest request) {
        QVisualizationTemplate visualizationTemplate = QVisualizationTemplate.visualizationTemplate;
        QVisualizationTemplateCategoryMap qVisualizationTemplateCategoryMap = QVisualizationTemplateCategoryMap.visualizationTemplateCategoryMap;
        Long result = queryFactory.selectFrom(visualizationTemplate)
                .leftJoin(qVisualizationTemplateCategoryMap).on(visualizationTemplate.id.eq(qVisualizationTemplateCategoryMap.templateId))
                .where(visualizationTemplate.name.in(request.getTemplateNames())
                        .and(qVisualizationTemplateCategoryMap.categoryId.in(request.getCategories()))
                        .and(visualizationTemplate.id.notIn(request.getTemplateArray()))).fetchCount();

        if (result == 0) {
            return CommonConstants.CHECK_RESULT.NONE;
        } else {
            return CommonConstants.CHECK_RESULT.EXIST_ALL;
        }
    }

    //分类名称检查
    public String categoryNameCheck(String optType, String name, String id) {
        Specification<VisualizationTemplateCategory> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (CommonConstants.OPT_TYPE.INSERT.equals(optType)) {
                predicates.add(cb.equal(root.get("name"), name));
            } else if (CommonConstants.OPT_TYPE.UPDATE.equals(optType)) {
                predicates.add(cb.equal(root.get("name"), name));
                predicates.add(cb.notEqual(root.get("id"), id));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<VisualizationTemplateCategory> templateList = visualizationTemplateCategoryRepository.findAll(spec);
        if (CollectionUtils.isEmpty(templateList)) {
            return CommonConstants.CHECK_RESULT.NONE;
        } else {
            return CommonConstants.CHECK_RESULT.EXIST_ALL;
        }
    }

    @Override
    public String nameCheck(TemplateManageRequest request) {
        return nameCheck(request.getOptType(), request.getName(), request.getId());
    }

    @Override
    public void delete(String id, String categoryId) {
        Assert.notNull(id, "id cannot be null");
        Assert.notNull(categoryId, "categoryId cannot be null");
        visualizationTemplateCategoryMapRepository.deleteByTemplateIdAndCategoryId(id, categoryId);
        // 如何是最后一个 则实际模板需要删除
        Specification<VisualizationTemplateCategoryMap> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("categoryId"), categoryId));
            predicates.add(cb.equal(root.get("templateId"), id));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        long result = visualizationTemplateCategoryMapRepository.count(spec);


        if (result == 0) {
            visualizationTemplateRepository.deleteById(id);
        }
    }

    @Override
    public String deleteCategory(String id) {
        Assert.notNull(id, "id cannot be null");
        // 该分类下是否有其他分类公用的模板
        Specification<VisualizationTemplateCategoryMap> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("categoryId"), id));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        long checkResult = visualizationTemplateCategoryMapRepository.count(spec);

        if (checkResult == 0) {
            visualizationTemplateCategoryRepository.deleteById(id);
            visualizationTemplateCategoryMapRepository.deleteByCategoryId(id);
            return "success";
        } else {
            return "repeat";
        }
    }

    @Override
    public VisualizationTemplateVO findOne(String templateId) {
        VisualizationTemplate template = visualizationTemplateRepository.findById(templateId).orElse(null);
        if (template != null) {
            VisualizationTemplateVO templateVO = new VisualizationTemplateVO();
            BeanUtils.copyBean(templateVO, template);
            //查找分类
            QVisualizationTemplateCategoryMap qVisualizationTemplateCategoryMap = QVisualizationTemplateCategoryMap.visualizationTemplateCategoryMap;

            List<String> categories = queryFactory
                    .select(qVisualizationTemplateCategoryMap.categoryId)
                    .from(qVisualizationTemplateCategoryMap)
                    .where(qVisualizationTemplateCategoryMap.templateId.eq(templateId)).fetch();

            templateVO.setCategories(categories);
            return templateVO;
        } else {
            return null;
        }
    }

    @Override
    public List<String> findCategoriesByTemplateIds(TemplateManageRequest request) throws Exception {
        if (!CollectionUtils.isEmpty(request.getTemplateArray())) {
            List<String> result = extTemplateMapper.findTemplateArrayCategories(request.getTemplateArray());
            if (!CollectionUtils.isEmpty(result) && result.size() == 1) {
                return Arrays.stream(result.get(0).split(",")).toList();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public List<TemplateManageDTO> find(TemplateManageRequest request) {
        return extTemplateMapper.findTemplateList(request);
    }

    @Override
    public List<TemplateManageDTO> findCategories(TemplateManageRequest request) {
        QVisualizationTemplateCategory visualizationTemplateCategory = QVisualizationTemplateCategory.visualizationTemplateCategory;
        return queryFactory.select(Projections.constructor(TemplateManageDTO.class,
                        visualizationTemplateCategory.id,
                        visualizationTemplateCategory.name,
                        visualizationTemplateCategory.name.as("label"),
                        visualizationTemplateCategory.pid,
                        visualizationTemplateCategory.level,
                        visualizationTemplateCategory.dvType,
                        visualizationTemplateCategory.nodeType,
                        visualizationTemplateCategory.createBy,
                        visualizationTemplateCategory.createTime,
                        visualizationTemplateCategory.templateType,
                        visualizationTemplateCategory.snapshot)).from(visualizationTemplateCategory)
                .orderBy(visualizationTemplateCategory.createTime.desc()).fetch();
    }

    @Override
    public void batchUpdate(TemplateManageBatchRequest request) {
        request.getTemplateIds().forEach(templateId -> {
            // 分类映射删除
            visualizationTemplateCategoryMapRepository.deleteByTemplateId(templateId);
            // 插入分类关系
            request.getCategories().forEach(categoryId -> {
                VisualizationTemplateCategoryMap categoryMap = new VisualizationTemplateCategoryMap();
                categoryMap.setId(UUID.randomUUID().toString());
                categoryMap.setCategoryId(categoryId);
                categoryMap.setTemplateId(templateId);
                visualizationTemplateCategoryMapRepository.saveAndFlush(categoryMap);
            });
        });
    }

    @Override
    public void batchDelete(TemplateManageBatchRequest request) {
        request.getTemplateIds().forEach(templateId -> {
            request.getCategories().forEach(categoryId -> {
                visualizationTemplateCategoryMapRepository.deleteByTemplateIdAndCategoryId(templateId, categoryId);
                // 如何是最后一个 则实际模板需要删除

                Specification<VisualizationTemplateCategoryMap> spec = (root, query, cb) -> {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(cb.equal(root.get("categoryId"), categoryId));
                    predicates.add(cb.equal(root.get("templateId"), templateId));
                    return cb.and(predicates.toArray(new Predicate[0]));
                };

                long result = visualizationTemplateCategoryMapRepository.count(spec);

                if (result == 0) {
                    visualizationTemplateRepository.deleteById(templateId);
                }
            });

        });
    }
}


