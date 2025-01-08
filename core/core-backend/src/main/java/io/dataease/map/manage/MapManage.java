package io.dataease.map.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.map.dto.GeometryNodeCreator;
import io.dataease.api.map.vo.AreaNode;
import io.dataease.api.map.vo.CustomGeoArea;
import io.dataease.api.map.vo.CustomGeoSubArea;
import io.dataease.constant.StaticResourceConstants;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import io.dataease.map.bo.AreaBO;
import io.dataease.map.dao.auto.entity.Area;
import io.dataease.map.dao.auto.entity.CoreCustomGeoArea;
import io.dataease.map.dao.auto.entity.CoreCustomGeoSubArea;
import io.dataease.map.dao.auto.mapper.AreaMapper;
import io.dataease.map.dao.auto.mapper.CoreCustomGeoAreaMapper;
import io.dataease.map.dao.auto.mapper.CoreCustomGeoSubAreaMapper;
import io.dataease.map.dao.ext.entity.CoreAreaCustom;
import io.dataease.map.dao.ext.mapper.CoreAreaCustomMapper;
import io.dataease.utils.*;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static io.dataease.constant.CacheConstant.CommonCacheConstant.CUSTOM_GEO_CACHE;
import static io.dataease.constant.CacheConstant.CommonCacheConstant.WORLD_MAP_CACHE;

@Component
public class MapManage {
    private final static AreaNode WORLD;

    private static final String GEO_PREFIX = "geo_";

    static {
        WORLD = AreaNode.builder()
                .id("000")
                .level("world")
                .name("世界村")
                .build();
    }

    @Resource
    private AreaMapper areaMapper;

    @Resource
    private CoreCustomGeoAreaMapper coreCustomGeoAreaMapper;

    @Resource
    private CoreCustomGeoSubAreaMapper coreCustomGeoSubAreaMapper;

    @Resource
    private CoreAreaCustomMapper coreAreaCustomMapper;

    public List<Area> defaultArea() {
        return areaMapper.selectList(null);
    }

    private MapManage proxy() {
        return CommonBeanFactory.getBean(MapManage.class);
    }

    @Cacheable(value = WORLD_MAP_CACHE, key = "'world_map'")
    public AreaNode getWorldTree() {
        List<Area> areas = proxy().defaultArea();
        List<AreaBO> areaBOS = areas.stream().map(item -> BeanUtils.copyBean(new AreaBO(), item)).collect(Collectors.toList());
        List<CoreAreaCustom> coreAreaCustoms = coreAreaCustomMapper.selectList(null);
        if (CollectionUtils.isNotEmpty(coreAreaCustoms)) {
            List<AreaBO> customBoList = coreAreaCustoms.stream().map(item -> {
                AreaBO areaBO = BeanUtils.copyBean(new AreaBO(), item);
                areaBO.setCustom(true);
                return areaBO;
            }).toList();
            areaBOS.addAll(customBoList);
        }
        WORLD.setChildren(new ArrayList<>());
        var areaNodeMap = new HashMap<String, AreaNode>();
        areaNodeMap.put(WORLD.getId(), WORLD);
        areaBOS.forEach(area -> {
            var node = areaNodeMap.get(area.getId());
            if (node == null) {
                node = AreaNode.builder().build();
                BeanUtils.copyBean(node, area);
                areaNodeMap.put(area.getId(), node);
            } else {
                BeanUtils.copyBean(node, area);
            }
            var pNode = areaNodeMap.get(area.getPid());
            if (pNode == null) {
                var child = new ArrayList<AreaNode>();
                child.add(node);
                pNode = AreaNode.builder()
                        .children(child)
                        .id(area.getPid())
                        .build();
                areaNodeMap.put(area.getPid(), pNode);
            } else {
                if (pNode.getChildren() == null) {
                    pNode.setChildren(new ArrayList<>());
                }
                pNode.getChildren().add(node);
            }
        });
        return WORLD;
    }

    @CacheEvict(cacheNames = WORLD_MAP_CACHE, key = "'world_map'")
    @Transactional
    public void saveMapGeo(GeometryNodeCreator request, MultipartFile file) {
        validateCode(request.getCode());
        if (ObjectUtils.isEmpty(file) || file.isEmpty()) {
            DEException.throwException("geometry file is require");
        }

        String suffix = FileUtils.getExtensionName(file.getOriginalFilename());
        if (!StringUtils.equalsIgnoreCase("json", suffix)) {
            DEException.throwException("仅支持json格式文件");
        }
        List<Area> areas = proxy().defaultArea();
        String code = getBusiGeoCode(request.getCode());

        AtomicReference<String> atomicReference = new AtomicReference<>();
        if (areas.stream().anyMatch(area -> {
            boolean exist = area.getId().equals(code);
            if (exist) {
                atomicReference.set(area.getName());
            }
            return exist;
        })) {
            DEException.throwException(String.format("Area code [%s] is already exists for [%s]", code, atomicReference.get()));
        }

        CoreAreaCustom originData = null;
        if (ObjectUtils.isNotEmpty(originData = coreAreaCustomMapper.selectById(getDaoGeoCode(code)))) {
            DEException.throwException(String.format("Area code [%s] is already exists for [%s]", code, originData.getName()));
        }

        CoreAreaCustom coreAreaCustom = new CoreAreaCustom();
        coreAreaCustom.setId(getDaoGeoCode(code));
        coreAreaCustom.setPid(request.getPid());
        coreAreaCustom.setName(request.getName());
        coreAreaCustomMapper.insert(coreAreaCustom);

        File geoFile = buildGeoFile(code);
        try {
            file.transferTo(geoFile);
        } catch (IOException e) {
            LogUtil.error(e.getMessage());
            DEException.throwException(e);
        }
    }

    @CacheEvict(cacheNames = WORLD_MAP_CACHE, key = "'world_map'")
    @Transactional
    public void deleteGeo(String code) {
        validateCode(code);
        if (!StringUtils.startsWith(code, GEO_PREFIX)) {
            DEException.throwException("内置Geometry，禁止删除");
        }
        CoreAreaCustom coreAreaCustom = coreAreaCustomMapper.selectById(code);
        if (ObjectUtils.isEmpty(coreAreaCustom)) {
            DEException.throwException("Geometry code 不存在！");
        }
        List<String> codeResultList = new ArrayList<>();
        codeResultList.add(code);
        childTreeIdList(List.of(code), codeResultList);
        coreAreaCustomMapper.deleteBatchIds(codeResultList);
        codeResultList.forEach(id -> {
            File file = buildGeoFile(id);
            if (file.exists()) {
                file.delete();
            }
        });
    }

    @Cacheable(value = CUSTOM_GEO_CACHE, key = "'custom_geo_area'")
    public List<CustomGeoArea> listCustomGeoArea() {
        return coreCustomGeoAreaMapper.selectList(null).stream().map(o -> BeanUtils.copyBean(new CustomGeoArea(), o)).toList();
    }

    public List<CustomGeoSubArea> getCustomGeoArea(String areaId) {
        var query = new QueryWrapper<CoreCustomGeoSubArea>();
        query.eq("geo_area_id", areaId);
        return coreCustomGeoSubAreaMapper.selectList(query).stream().map(o -> BeanUtils.copyBean(new CustomGeoSubArea(), o)).toList();
    }

    @CacheEvict(cacheNames = CUSTOM_GEO_CACHE, key = "'custom_geo_area'")
    @Transactional
    public void deleteCustomGeoArea(String areaId) {
        coreCustomGeoAreaMapper.deleteById(areaId);
        var q = new QueryWrapper<CoreCustomGeoSubArea>();
        q.eq("geo_area_id", areaId);
        coreCustomGeoSubAreaMapper.delete(q);
    }

    @CacheEvict(cacheNames = CUSTOM_GEO_CACHE, key = "'custom_geo_area'")
    @Transactional
    public void saveCustomGeoArea(CustomGeoArea geoArea) {
        var coreCustomGeoArea = new CoreCustomGeoArea();
        BeanUtils.copyBean(coreCustomGeoArea, geoArea);
        var q = new QueryWrapper<CoreCustomGeoArea>();
        q.eq("name", geoArea.getName());
        if (StringUtils.isNotBlank(coreCustomGeoArea.getId())) {
            q.ne("id", coreCustomGeoArea.getId());
        }
        var list = coreCustomGeoAreaMapper.selectList(q);
        if (CollectionUtils.isNotEmpty(list)) {
            DEException.throwException(Translator.get("i18n_geo_exists"));
            return;
        }
        if (ObjectUtils.isEmpty(coreCustomGeoArea.getId())) {
            coreCustomGeoArea.setId("custom_" + IDUtils.snowID());
            coreCustomGeoAreaMapper.insert(coreCustomGeoArea);
        } else {
            coreCustomGeoAreaMapper.updateById(coreCustomGeoArea);
        }
    }

    @Transactional
    public void deleteCustomGeoSubArea(long areaId) {
        coreCustomGeoSubAreaMapper.deleteById(areaId);
    }

    @Transactional
    public void saveCustomGeoSubArea(CustomGeoSubArea customGeoSubArea) {
        var geoSubArea = new CoreCustomGeoSubArea();
        BeanUtils.copyBean(geoSubArea, customGeoSubArea);
        var q = new QueryWrapper<CoreCustomGeoSubArea>();
        q.eq("name", customGeoSubArea.getName());
        q.eq("geo_area_id", customGeoSubArea.getGeoAreaId());
        if (ObjectUtils.isNotEmpty(customGeoSubArea.getId())) {
            q.ne("id", customGeoSubArea.getId());
        }
        var list = coreCustomGeoSubAreaMapper.selectList(q);
        if (CollectionUtils.isNotEmpty(list)) {
            DEException.throwException(Translator.get("i18n_geo_sub_exists"));
            return;
        }
        if (ObjectUtils.isEmpty(geoSubArea.getId())) {
            geoSubArea.setId(IDUtils.snowID());
            coreCustomGeoSubAreaMapper.insert(geoSubArea);
        } else {
            coreCustomGeoSubAreaMapper.updateById(geoSubArea);
        }
    }

    public List<AreaNode> getCustomGeoSubAreaOptions() {
        var q = new QueryWrapper<Area>();
        q.eq("pid", "156");
        return areaMapper.selectList(q).stream().map(a -> BeanUtils.copyBean(AreaNode.builder().build(), a)).toList();
    }

    public void childTreeIdList(List<String> pidList, List<String> resultList) {
        QueryWrapper<CoreAreaCustom> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("pid", pidList);
        List<CoreAreaCustom> coreAreaCustoms = coreAreaCustomMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(coreAreaCustoms)) {
            List<String> codeList = coreAreaCustoms.stream().map(CoreAreaCustom::getId).toList();
            resultList.addAll(codeList);
            childTreeIdList(codeList, resultList);
        }
    }

    private String getDaoGeoCode(String code) {
        return StringUtils.startsWith(code, GEO_PREFIX) ? code : (GEO_PREFIX + code);
    }

    private String getBusiGeoCode(String code) {
        return StringUtils.startsWith(code, GEO_PREFIX) ? code.substring(GEO_PREFIX.length()) : code;
    }

    private File buildGeoFile(String code) {
        String id = getBusiGeoCode(code);
        String customMapDir = StaticResourceConstants.CUSTOM_MAP_DIR;
        String countryCode = countryCode(id);
        String fileDirPath = customMapDir + "/" + countryCode + "/";
        File dir = new File(fileDirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String filePath = fileDirPath + id + ".json";
        return new File(filePath);
    }

    private String countryCode(String code) {
        return code.substring(0, 3);
    }

    public void validateCode(String code) {
        if (StringUtils.isBlank(code)) DEException.throwException("区域编码不能为空");
        String busiGeoCode = getBusiGeoCode(code);
        if (!isNumeric(busiGeoCode)) {
            DEException.throwException("有效区域编码只能是数字");
        }
    }

    public boolean isNumeric(String str) {
        for (int i = str.length(); --i >= 0; ) {
            int chr = str.charAt(i);
            if (chr < 48 || chr > 57)
                return false;
        }
        return true;
    }
}
