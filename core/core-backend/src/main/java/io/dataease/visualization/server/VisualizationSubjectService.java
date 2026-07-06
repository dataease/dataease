package io.dataease.visualization.server;

import io.dataease.api.visualization.VisualizationSubjectApi;
import io.dataease.api.visualization.request.VisualizationSubjectRequest;
import io.dataease.api.visualization.vo.VisualizationSubjectVO;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import io.dataease.visualization.dao.auto.entity.VisualizationSubject;
import io.dataease.visualization.dao.auto.mapper.VisualizationSubjectRepository;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : WangJiaHao
 * @date : 2023/6/9 18:41
 */
@RestController
@RequestMapping("/visualizationSubject")
public class VisualizationSubjectService implements VisualizationSubjectApi {

    @Resource
    VisualizationSubjectRepository visualizationSubjectRepository;

    /**
     * 系统内置主题名称的国际化 key，实际以 key 形式存库，展示时经过国际化转义。
     * 用户新建/重命名主题时，需将其转义后的名称一并纳入重复校验。
     */
    private static final List<String> DEFAULT_SUBJECT_NAME_KEYS = Arrays.asList("chart.light_theme", "chart.dark_theme");

    /**
     * 判断名称是否与系统内置主题的国际化名称重复
     */
    private boolean isDefaultSubjectName(String name) {
        if (StringUtils.isBlank(name)) {
            return false;
        }
        for (String key : DEFAULT_SUBJECT_NAME_KEYS) {
            if (StringUtils.equals(name, Translator.get(key))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<VisualizationSubjectVO> query(VisualizationSubjectRequest request) {
        Specification<VisualizationSubject> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("deleteFlag")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<VisualizationSubject> result = visualizationSubjectRepository.findAll(spec);
        return result.stream().map(subject -> {
            VisualizationSubjectVO subjectVO = new VisualizationSubjectVO();
            BeanUtils.copyBean(subject, subjectVO);
            return subjectVO;
        }).collect(Collectors.toList());
    }

    @Override
    public List querySubjectWithGroup(VisualizationSubjectRequest request) {
        List result = new ArrayList();
        int pageSize = 4;
        Sort sort = Sort.by(Sort.Direction.ASC, "createTime");
        List<VisualizationSubject> allInfo = visualizationSubjectRepository.findAll(sort);
        for (int i = 0; i < allInfo.size(); i = i + pageSize) {
            List<VisualizationSubject> tmp = allInfo.subList(i, Math.min(i + pageSize, allInfo.size()));
            result.add(tmp);
        }
        return result;
    }

    @Override
    public synchronized void update(VisualizationSubjectRequest request) {
        if (StringUtils.isEmpty(request.getId())) {
            Specification<VisualizationSubject> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("name"), request.getName()));
                return cb.and(predicates.toArray(new Predicate[0]));
            };
            List<VisualizationSubject> subjectAll = visualizationSubjectRepository.findAll(spec);
            if (CollectionUtils.isEmpty(subjectAll) && !isDefaultSubjectName(request.getName())) {
                request.setId(IDUtils.snowID().toString());
                request.setCreateTime(System.currentTimeMillis());
                request.setType("self");
                request.setName(request.getName());
                VisualizationSubject saveInfo = new VisualizationSubject();
                BeanUtils.copyBean(saveInfo, request);
                saveInfo.setCreateNum(0);
                visualizationSubjectRepository.saveAndFlush(saveInfo);
            } else {
                DEException.throwException("名称已经存在");
            }
        } else {
            Specification<VisualizationSubject> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("name"), request.getName()));
                predicates.add(cb.equal(root.get("id"), request.getId()));
                return cb.and(predicates.toArray(new Predicate[0]));
            };

            List<VisualizationSubject> subjectAll = visualizationSubjectRepository.findAll(spec);
            if (CollectionUtils.isEmpty(subjectAll) && !isDefaultSubjectName(request.getName())) {
                request.setUpdateTime(System.currentTimeMillis());
                VisualizationSubject updateInfo = new VisualizationSubject();
                BeanUtils.copyBean(updateInfo, request);
                visualizationSubjectRepository.saveAndFlush(updateInfo);
            } else {
                DEException.throwException("名称已经存在");
            }
        }
    }

    @Override
    public void delete(String id) {
        Assert.notNull(id, "subjectId should not be null");
        visualizationSubjectRepository.deleteById(id);
    }

}
