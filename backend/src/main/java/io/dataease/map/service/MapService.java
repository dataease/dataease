package io.dataease.map.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import io.dataease.commons.exception.DEException;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.listener.util.CacheUtils;
import io.dataease.map.dto.entity.AreaEntity;
import io.dataease.map.dto.request.MapNodeRequest;
import io.dataease.map.utils.MapUtils;
import io.dataease.plugins.common.base.domain.AreaMappingGlobal;
import io.dataease.plugins.common.base.domain.AreaMappingGlobalExample;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class MapService {

    @Value("${geo.custom.rootpath:/opt/dataease/data/custom/}")
    private String rootGeoPath;


    @Cacheable("sys_map_areas")
    public List<AreaEntity> areaEntities() {
        List<AreaEntity> areaEntities = MapUtils.readAreaEntity();
        return areaEntities;
    }

    @Cacheable("sys_map_areas_global")
    public List<AreaEntity> globalEntities() {
        List<AreaEntity> areaEntities = MapUtils.readGlobalAreaEntity();
        return areaEntities;
    }

    public List<AreaEntity> entitiesByPid(List<AreaEntity> entities, String pid) {
        for (int i = 0; i < entities.size(); i++) {
            AreaEntity areaEntity = entities.get(i);
            if (StringUtils.equals(pid, areaEntity.getCode())) {
                return areaEntity.getChildren();
            }

            if (CollectionUtil.isNotEmpty(areaEntity.getChildren())) {
                List<AreaEntity> areaEntities = entitiesByPid(areaEntity.getChildren(), pid);
                if (null != areaEntities) {
                    return areaEntities;
                }
            }
        }
        return null;

    }

    public String generateAreaCode(String pCode) {
        Long pValue = Long.parseLong(pCode);
        MapService mapService = CommonBeanFactory.getBean(MapService.class);
        List<AreaEntity> areaEntities = mapService.globalEntities();
        List<AreaEntity> brothers = entitiesByPid(areaEntities, pCode);

        brothers.sort(Comparator.comparing(item -> Long.parseLong(item.getCode())));
        AreaEntity lastBrother = brothers.get(brothers.size() - 1);

        Long lastCode = Long.parseLong(lastBrother.getCode());

        long areaCodeSuffix = lastCode - pValue;

        int step = 0;

        while (areaCodeSuffix % 10 == 0) {
            step++;
            areaCodeSuffix /= 10;
        }

        areaCodeSuffix++;

        while (step > 0) {
            areaCodeSuffix *= 10;
            step--;
        }

        long targetCode = areaCodeSuffix + pValue;
        return String.valueOf(targetCode);
    }

    private void delFileByNodes(List<AreaMappingGlobal> nodes, Integer pLevel) {
        Set<String> sets = nodes.stream().flatMap(node -> codesByNode(node, pLevel).stream()).collect(Collectors.toSet());
        sets.forEach(code -> {
            String countryCode = code.substring(0, 3);
            String path = rootGeoPath + "/full/" + countryCode + "/" + code +"_full.json";
            if (FileUtil.exist(path)) {
                FileUtil.del(path);
            }
        });
    }

    private Set<String> codesByNode(AreaMappingGlobal node, Integer pLevel) {
        Set<String> sets = new TreeSet<>();

        if (pLevel == 2) {
            if(StringUtils.isNotBlank(node.getProvinceCode())) sets.add(node.getProvinceCode());
            if(StringUtils.isNotBlank(node.getCityCode())) sets.add(node.getCityCode());
            if(StringUtils.isNotBlank(node.getCountyCode())) sets.add(node.getCountyCode());
        } else if (pLevel == 3) {
            if(StringUtils.isNotBlank(node.getCityCode())) sets.add(node.getCityCode());
            if(StringUtils.isNotBlank(node.getCountyCode())) sets.add(node.getCountyCode());
        } else if (pLevel == 4) {
            if(StringUtils.isNotBlank(node.getCountyCode())) sets.add(node.getCountyCode());
        } else {
            if(StringUtils.isNotBlank(node.getCountryCode())) sets.add(node.getCountryCode());
            if(StringUtils.isNotBlank(node.getProvinceCode())) sets.add(node.getProvinceCode());
            if(StringUtils.isNotBlank(node.getCityCode())) sets.add(node.getCityCode());
            if(StringUtils.isNotBlank(node.getCountyCode())) sets.add(node.getCountyCode());
        }
        return sets;
    }

    @Transactional
    public void delMapNode(MapNodeRequest request) {
        String pCode = request.getPcode();
        Integer pLevel = request.getPlevel();
        String code = request.getCode();
        AreaMappingGlobalExample example = new AreaMappingGlobalExample();
        AreaMappingGlobal curRoot = new AreaMappingGlobal();
        List<AreaMappingGlobal> nodes = null;
        if(pLevel == 0) {
            nodes = MapUtils.selectByExample(example);
            MapUtils.deleteByExample(example);
            delFileByNodes(nodes, pLevel);
        } else if (pLevel == 1) {
            example.createCriteria().andCountryCodeEqualTo(code);
            nodes = MapUtils.selectByExample(example);
            MapUtils.deleteByExample(example);
            delFileByNodes(nodes, pLevel);
        } else if (pLevel == 2) {
            example.createCriteria().andCountryCodeEqualTo(pCode).andProvinceCodeEqualTo(code);
            nodes = MapUtils.selectByExample(example);
            MapUtils.deleteByExample(example);
            example.clear();
            example.createCriteria().andCountryCodeEqualTo(pCode);
            if (!MapUtils.exampleExist(example) && CollectionUtil.isNotEmpty(nodes)) {
                AreaMappingGlobal template = nodes.get(0);
                curRoot.setCountryCode(template.getCountryCode());
                curRoot.setCountryName(template.getCountryName());
                MapUtils.addNode(curRoot);
            }
            delFileByNodes(nodes, pLevel);
        } else if (pLevel == 3) {

            example.createCriteria().andProvinceCodeEqualTo(pCode).andCityCodeEqualTo(code);
            nodes = MapUtils.selectByExample(example);

            MapUtils.deleteByExample(example);
            example.clear();
            example.createCriteria().andProvinceCodeEqualTo(pCode);

            if (!MapUtils.exampleExist(example) && CollectionUtil.isNotEmpty(nodes)) {
                AreaMappingGlobal template = nodes.get(0);
                curRoot.setCountryCode(template.getCountryCode());
                curRoot.setCountryName(template.getCountryName());
                curRoot.setProvinceCode(template.getProvinceCode());
                curRoot.setProvinceName(template.getProvinceName());
                MapUtils.addNode(curRoot);
            }
            delFileByNodes(nodes, pLevel);
        } else if (pLevel == 4) {
            example.createCriteria().andCityCodeEqualTo(pCode).andCountyCodeEqualTo(code);
            nodes = MapUtils.selectByExample(example);

            MapUtils.deleteByExample(example);
            example.clear();
            example.createCriteria().andProvinceCodeEqualTo(pCode);

            if (!MapUtils.exampleExist(example) && CollectionUtil.isNotEmpty(nodes)) {
                AreaMappingGlobal template = nodes.get(0);
                curRoot.setCountryCode(template.getCountryCode());
                curRoot.setCountryName(template.getCountryName());
                curRoot.setProvinceCode(template.getProvinceCode());
                curRoot.setProvinceName(template.getProvinceName());
                curRoot.setCityCode(template.getCityCode());
                curRoot.setCityName(template.getCityName());
                MapUtils.addNode(curRoot);
            }
            delFileByNodes(nodes, pLevel);
        }
        CacheUtils.removeAll("sys_map_areas_global");
    }

    private void validateFile(MultipartFile file) {
        long size = file.getSize();
        String name = file.getOriginalFilename();
        if (size / 1024 / 1024 > 30) {
            DEException.throwException("large file that exceed 30M is not supported");
        }
        if (!StringUtils.endsWith(name, ".json")) {
            DEException.throwException("only json file supported");
        }
    }
    @Transactional
    public void saveMapNode(MapNodeRequest request, MultipartFile file) throws Exception{
        validateFile(file);
        String pCode = request.getPcode();
        Integer plevel = request.getPlevel();
        String code = request.getCode();

        if(StringUtils.isBlank(code)) {
            String newAreaCode = generateAreaCode(pCode);
            request.setCode(newAreaCode);
        }

        AreaMappingGlobalExample example = new AreaMappingGlobalExample();


        if (plevel == 1) {
            example.createCriteria().andCountryCodeEqualTo(code);
        }
        else if (plevel == 2) {
            example.createCriteria().andCountryCodeEqualTo(pCode).andProvinceCodeEqualTo(code);
        }
        else if (plevel == 3) {
            example.createCriteria().andProvinceCodeEqualTo(pCode).andCityCodeEqualTo(code);
        }else if (plevel == 4) {
            example.createCriteria().andCityCodeEqualTo(pCode).andCountyCodeEqualTo(code);
        } else {
            DEException.throwException("只支持3级行政区");
        }
        List<AreaMappingGlobal> lists = MapUtils.selectByExample(example);
        if (CollectionUtil.isNotEmpty(lists)) {
            DEException.throwException("区域代码已存在");
        }

        example.clear();
        AreaMappingGlobalExample pExample = new AreaMappingGlobalExample();
        if (plevel == 1) {
            pExample.createCriteria().andCountryCodeIsNull().andProvinceCodeIsNull().andCityCodeIsNull().andCountyCodeIsNull();
            List<AreaMappingGlobal> existLists = MapUtils.selectByExample(pExample);
            if (CollectionUtil.isNotEmpty(existLists)) {
                AreaMappingGlobal node = existLists.get(0);
                node.setCountryCode(code);
                node.setCountryName(request.getName());
                MapUtils.update(node);
            }else {
                AreaMappingGlobal node = new AreaMappingGlobal();
                node.setCountryCode(code);
                node.setCountryName(request.getName());
                MapUtils.addNode(node);
            }
        }
        else if (plevel == 2) {
            pExample.createCriteria().andCountryCodeEqualTo(pCode).andProvinceCodeIsNull().andCityCodeIsNull().andCountyCodeIsNull();
            List<AreaMappingGlobal> existLists = MapUtils.selectByExample(pExample);
            if (CollectionUtil.isNotEmpty(existLists)) {
                AreaMappingGlobal node = existLists.get(0);
                node.setProvinceCode(code);
                node.setProvinceName(request.getName());
                MapUtils.update(node);
            }else {
                AreaMappingGlobal country = country(pCode);
                AreaMappingGlobal node = new AreaMappingGlobal();
                node.setCountryCode(pCode);
                node.setCountryName(country.getCountryName());
                node.setProvinceCode(code);
                node.setProvinceName(request.getName());
                MapUtils.addNode(node);
            }
        }
        else if (plevel == 3) {
            pExample.createCriteria().andProvinceCodeEqualTo(pCode).andCityCodeIsNull();
            List<AreaMappingGlobal> existLists = MapUtils.selectByExample(pExample);
            if (CollectionUtil.isNotEmpty(existLists)) {
                AreaMappingGlobal node = existLists.get(0);
                node.setCityCode(code);
                node.setCityName(request.getName());
                MapUtils.update(node);
            }else {
                AreaMappingGlobal province = province(pCode);
                AreaMappingGlobal node = new AreaMappingGlobal();
                node.setCountryCode(province.getCountryCode());
                node.setCountryName(province.getCountryName());
                node.setProvinceCode(pCode);
                node.setProvinceName(province.getProvinceName());
                node.setCityCode(code);
                node.setCityName(request.getName());
                MapUtils.addNode(node);
            }
        } else if (plevel == 4) {
            pExample.createCriteria().andCountryCodeEqualTo(pCode).andCountyCodeIsNull();
            List<AreaMappingGlobal> existLists = MapUtils.selectByExample(pExample);
            if (CollectionUtil.isNotEmpty(existLists)) {
                AreaMappingGlobal node = existLists.get(0);
                node.setCountyCode(code);
                node.setCountyName(request.getName());
                MapUtils.update(node);
            }else {
                AreaMappingGlobal city = city(pCode);
                AreaMappingGlobal node = new AreaMappingGlobal();
                node.setCountryCode(city.getCountryCode());
                node.setCountryName(city.getCountryName());
                node.setProvinceCode(city.getProvinceCode());
                node.setProvinceName(city.getProvinceName());
                node.setCityCode(pCode);
                node.setCityName(request.getName());
                node.setCountyCode(code);
                node.setCountyName(request.getName());
                MapUtils.addNode(node);
            }
        } else {
            DEException.throwException("只支持3级行政区");
        }
        uploadMapFile(file, code);
        CacheUtils.removeAll("sys_map_areas_global");
    }

    public void uploadMapFile(MultipartFile file, String areaCode) throws Exception{

        String countryCode = areaCode.substring(0, 3);
        String dir = rootGeoPath + "full/" + countryCode + "/";
        File fileDir = new File(dir);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }

        String targetPath = dir + areaCode+"_full.json";
        File target = new File(targetPath);
        file.transferTo(target);
    }

    private AreaMappingGlobal country(String pCode) {
        AreaMappingGlobalExample pExample = new AreaMappingGlobalExample();
        pExample.createCriteria().andCountryCodeEqualTo(pCode);
        return MapUtils.selectByExample(pExample).stream().findFirst().get();
    }

    private AreaMappingGlobal province(String pCode) {
        AreaMappingGlobalExample pExample = new AreaMappingGlobalExample();
        pExample.createCriteria().andProvinceCodeEqualTo(pCode);
        return MapUtils.selectByExample(pExample).stream().findFirst().get();
    }

    private AreaMappingGlobal city(String pCode) {
        AreaMappingGlobalExample pExample = new AreaMappingGlobalExample();
        pExample.createCriteria().andCityCodeEqualTo(pCode);
        return MapUtils.selectByExample(pExample).stream().findFirst().get();
    }

}
