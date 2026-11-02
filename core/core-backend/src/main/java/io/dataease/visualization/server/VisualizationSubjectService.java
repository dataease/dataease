package io.dataease.visualization.server;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.visualization.VisualizationSubjectApi;
import io.dataease.api.visualization.request.VisualizationSubjectRequest;
import io.dataease.api.visualization.vo.VisualizationSubjectVO;
import io.dataease.exception.DEException;
import io.dataease.i18n.Lang;
import io.dataease.i18n.Translator;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import io.dataease.visualization.dao.auto.entity.VisualizationSubject;
import io.dataease.visualization.dao.auto.mapper.VisualizationSubjectMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * @author : WangJiaHao
 * @date : 2023/6/9 18:41
 */
@RestController
@RequestMapping("/visualizationSubject")
public class VisualizationSubjectService implements VisualizationSubjectApi {

    @Resource
    VisualizationSubjectMapper subjectMapper;

    @Resource
    private MessageSource messageSource;

    @Override
    public List<VisualizationSubjectVO> query(VisualizationSubjectRequest request) {
        QueryWrapper<VisualizationSubject> wrapper = new QueryWrapper<>();
        wrapper.eq("delete_flag", 0);
        List<VisualizationSubject> result =subjectMapper.selectList(wrapper);
        return result.stream().map(subject ->{
            VisualizationSubjectVO subjectVO = new VisualizationSubjectVO();
            BeanUtils.copyBean(subject,subjectVO);
            return subjectVO;
        }).collect(Collectors.toList());
    }

    @Override
    public List querySubjectWithGroup(VisualizationSubjectRequest request) {
        List result = new ArrayList();
        int pageSize = 4;
        QueryWrapper<VisualizationSubject> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("create_time");
        List<VisualizationSubject> allInfo =subjectMapper.selectList(wrapper);
        for (int i = 0; i < allInfo.size(); i = i + pageSize) {
            List<VisualizationSubject> tmp = allInfo.subList(i, Math.min(i + pageSize, allInfo.size()));
            result.add(tmp);
        }
        return result;
    }
    @Override
    public synchronized void update(VisualizationSubjectRequest request) {
        checkName(request);
        if (StringUtils.isEmpty(request.getId())) {
            request.setId(IDUtils.snowID().toString());
            request.setCreateTime(System.currentTimeMillis());
            request.setType("self");
            VisualizationSubject saveInfo = new VisualizationSubject();
            BeanUtils.copyBean(saveInfo, request);
            subjectMapper.insert(saveInfo);
        } else {
            request.setUpdateTime(System.currentTimeMillis());
            VisualizationSubject updateInfo = new VisualizationSubject();
            BeanUtils.copyBean(updateInfo, request);
            subjectMapper.updateById(updateInfo);
        }
    }

    private void checkName(VisualizationSubjectRequest request) {
        // System themes store i18n keys, so also reserve their displayed names in every supported language.
        String name = StringUtils.trim(request.getName());
        for (Lang lang : Lang.values()) {
            Locale locale = Locale.forLanguageTag(lang.getDesc());
            for (String key : List.of("i18n_subject_light_theme", "i18n_subject_dark_theme")) {
                if (StringUtils.equalsIgnoreCase(name, messageSource.getMessage(key, null, locale))) {
                    DEException.throwException(Translator.get("i18n_subject_name_exists"));
                }
            }
        }
        QueryWrapper<VisualizationSubject> wrapper = new QueryWrapper<>();
        wrapper.eq("name", request.getName());
        if (StringUtils.isNotEmpty(request.getId())) {
            wrapper.ne("id", request.getId());
        }
        if (!CollectionUtils.isEmpty(subjectMapper.selectList(wrapper))) {
            DEException.throwException(Translator.get("i18n_subject_name_exists"));
        }
    }

    @Override
    public void delete(String id) {
        Assert.notNull(id, "subjectId should not be null");
        subjectMapper.deleteById(id);
    }
}
