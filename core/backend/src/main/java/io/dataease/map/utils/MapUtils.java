package io.dataease.map.utils;

import cn.hutool.core.util.StrUtil;
import io.dataease.map.dto.entity.AreaEntity;
import io.dataease.map.dto.entity.Constants;
import io.dataease.plugins.common.base.domain.AreaMapping;
import io.dataease.plugins.common.base.domain.AreaMappingExample;
import io.dataease.plugins.common.base.domain.AreaMappingGlobal;
import io.dataease.plugins.common.base.domain.AreaMappingGlobalExample;
import io.dataease.plugins.common.base.mapper.AreaMappingGlobalMapper;
import io.dataease.plugins.common.base.mapper.AreaMappingMapper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class MapUtils {

    private static AreaMappingMapper areaMappingMapper;

    private static AreaMappingGlobalMapper areaMappingGlobalMapper;

    @Autowired
    public void setAreaMappingMapper(AreaMappingMapper areaMappingMapper) {
        MapUtils.areaMappingMapper = areaMappingMapper;
    }

    @Autowired
    public void setAreaMappingGlobalMapper(AreaMappingGlobalMapper areaMappingGlobalMapper) {
        MapUtils.areaMappingGlobalMapper = areaMappingGlobalMapper;
    }

    private static final String featureDir = "/opt/dataease/data/feature/";

    public static String formatCode(String code) {
        return code;
    }

    public static List<AreaMappingGlobal> readGlobalCodes() {
        AreaMappingGlobalExample example = new AreaMappingGlobalExample();
        List<AreaMappingGlobal> mappingGlobals = areaMappingGlobalMapper.selectByExample(example);
        return mappingGlobals;
    }

    public static List<AreaMappingGlobal> selectByExample(AreaMappingGlobalExample example) {
        List<AreaMappingGlobal> mappingGlobals = areaMappingGlobalMapper.selectByExample(Optional.ofNullable(example).orElse(new AreaMappingGlobalExample()));
        return mappingGlobals;
    }

    public static Boolean exampleExist(AreaMappingGlobalExample example) {
        return areaMappingGlobalMapper.countByExample(example) > 0;
    }

    public static List<Map<String, Object>> readCodeList() {
        AreaMappingExample example = new AreaMappingExample();
        List<AreaMapping> areaMappings = areaMappingMapper.selectByExample(example);
        return areaMappings.stream().map(mapping -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", mapping.getId());
            map.put(Constants.PROVINCE_NAME, mapping.getProvinceName());
            map.put(Constants.PROVINCE_CODE, mapping.getProvinceCode());
            map.put(Constants.CITY_NAME, mapping.getCityName());
            map.put(Constants.CITY_CODE, mapping.getCityCode());
            map.put(Constants.COUNTY_NAME, mapping.getCountyName());
            map.put(Constants.COUNTY_CODE, mapping.getCountyCode());
            return map;
        }).collect(Collectors.toList());
    }

    public static List<AreaEntity> readGlobalAreaEntity() {
        List<AreaMappingGlobal> maps = readGlobalCodes();
        Map<String, AreaEntity> countryMap = new ConcurrentHashMap<>();
        Map<String, AreaEntity> provinceMap = new ConcurrentHashMap<>();
        Map<String, AreaEntity> cityMap = new ConcurrentHashMap<>();
        Map<String, AreaEntity> countyMap = new ConcurrentHashMap<>();
        AreaEntity globalRoot = globalRoot();
        maps.stream().forEach(map -> {
            String country_code = map.getCountryCode();
            String province_code = map.getProvinceCode();
            String city_code = map.getCityCode();
            String county_code = map.getCountyCode();
            // 是否是跨级直辖
            Boolean isCrossLevel = StrUtil.equals(province_code, city_code) && !StrUtil.equals(province_code, "156710000");

            if (!countryMap.containsKey(country_code)) {
                String country_name = map.getCountryName();
                AreaEntity child = AreaEntity.builder().code(country_code).name(country_name).pcode(globalRoot.getCode()).build();
                countryMap.put(country_code, child);
                globalRoot.addChild(child);
            }


            if (StringUtils.isNotBlank(province_code) && !provinceMap.containsKey(province_code)) {
                AreaEntity currentCountry = countryMap.get(country_code);
                String province_name = map.getProvinceName();
                AreaEntity child = AreaEntity.builder().code(province_code).name(province_name).pcode(currentCountry.getCode()).build();
                provinceMap.put(province_code, child);
                currentCountry.addChild(child);
            }



            String city_name = map.getCityName();
            if (isCrossLevel) {
                city_code = county_code;
                city_name = map.getCountyName();
            }
            if (StringUtils.isNotBlank(city_code) && !cityMap.containsKey(city_code)) {
                // 当前省
                AreaEntity currentProvince = provinceMap.get(province_code);
                AreaEntity child = AreaEntity.builder().code(city_code).name(city_name).pcode(currentProvince.getCode()).build();
                cityMap.put(city_code, child);
                currentProvince.addChild(child);
            }
            if (StringUtils.isNotBlank(county_code) && !isCrossLevel) {
                // 当前市
                AreaEntity currentCity = cityMap.get(city_code);
                if (!countyMap.containsKey(county_code)) {
                    String county_name = map.getCountyName();
                    AreaEntity child = AreaEntity.builder().code(county_code).name(county_name)
                            .pcode(currentCity.getCode())
                            .build();
                    countyMap.put(county_code, child);
                    currentCity.addChild(child);
                }
            }
        });

        List<AreaEntity> result = new ArrayList<>();
        result.add(globalRoot);
        return result;
    }

    public static List<AreaEntity> readAreaEntity() {
        List<Map<String, Object>> maps = readCodeList();

        Map<String, AreaEntity> provinceMap = new ConcurrentHashMap<>();
        Map<String, AreaEntity> cityMap = new ConcurrentHashMap<>();
        Map<String, AreaEntity> countyMap = new ConcurrentHashMap<>();

        AreaEntity china = root();

        maps.stream().forEach(map -> {
            String province_code = map.get(Constants.PROVINCE_CODE).toString();
            String city_code = map.get(Constants.CITY_CODE).toString();
            String county_code = map.get(Constants.COUNTY_CODE).toString();

            province_code = formatCode(province_code);
            city_code = formatCode(city_code);
            county_code = formatCode(county_code);

            // 是否是跨级直辖
            Boolean isCrossLevel = StrUtil.equals(province_code, city_code) && !StrUtil.equals(province_code, "710000");

            if (!provinceMap.containsKey(province_code)) {
                String province_name = map.get(Constants.PROVINCE_NAME).toString();
                AreaEntity child = AreaEntity.builder().code(province_code).name(province_name).pcode(china.getCode())
                        .build();
                provinceMap.put(province_code, child);
                china.addChild(child);
            }

            // 当前省
            AreaEntity currentProvince = provinceMap.get(province_code);

            String city_name = map.get(Constants.CITY_NAME).toString();
            if (isCrossLevel) {
                city_code = county_code;
                city_name = map.get(Constants.COUNTY_NAME).toString();
            }
            if (!cityMap.containsKey(city_code)) {
                AreaEntity child = AreaEntity.builder().code(city_code).name(city_name).pcode(currentProvince.getCode())
                        .build();
                cityMap.put(city_code, child);
                currentProvince.addChild(child);
            }
            if (!isCrossLevel) {
                // 当前市
                AreaEntity currentCity = cityMap.get(city_code);
                if (!countyMap.containsKey(county_code)) {
                    String county_name = map.get(Constants.COUNTY_NAME).toString();
                    AreaEntity child = AreaEntity.builder().code(county_code).name(county_name)
                            .pcode(currentCity.getCode())
                            .build();
                    countyMap.put(county_code, child);
                    currentCity.addChild(child);
                }
            }
        });
        List<AreaEntity> result = new ArrayList<>();
        result.add(china);
        return result;
    }

    private static AreaEntity root() {
        return AreaEntity.builder().code("100000").name("中华人民共和国").build();
    }

    private static AreaEntity globalRoot() {
        return AreaEntity.builder().code("000000000").name("地球村").build();
    }

    public static void addNode(AreaMappingGlobal node) {
        areaMappingGlobalMapper.insert(node);
    }

    public static void update(AreaMappingGlobal node) {
        areaMappingGlobalMapper.updateByPrimaryKey(node);
    }

    public static void deleteByExample(AreaMappingGlobalExample example) {
        areaMappingGlobalMapper.deleteByExample(Optional.ofNullable(example).orElse(new AreaMappingGlobalExample()));
    }

}
