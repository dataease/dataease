package io.dataease.map.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.dataease.base.domain.AreaMapping;
import io.dataease.base.domain.AreaMappingExample;
import io.dataease.base.mapper.AreaMappingMapper;
import io.dataease.commons.utils.LogUtil;
import io.dataease.map.dto.entity.*;
import io.dataease.map.dto.entity.Properties;
import io.dataease.map.dto.response.MapResponse;
import io.dataease.map.dto.response.MapResultDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class MapUtils {


    private static AreaMappingMapper areaMappingMapper;

    @Autowired
    public void setAreaMappingMapper(AreaMappingMapper areaMappingMapper) {
        MapUtils.areaMappingMapper = areaMappingMapper;
    }

    private static final String path = "/opt/dataease/data/行政区划列表2020-03.xlsx";
    private static final String featureDir = "/opt/dataease/data/feature/";


    public static String formatCode(String code) {
        return code;
    }


    public static List<Map<String, Object>> readCodeList( ) {
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
                AreaEntity child = AreaEntity.builder().code(province_code).name(province_name).pcode(china.getCode()).build();
                provinceMap.put(province_code, child);
                china.addChild(child);
            }

            //当前省
            AreaEntity currentProvince = provinceMap.get(province_code);

            String city_name = map.get(Constants.CITY_NAME).toString();
            if (isCrossLevel) {
                city_code = county_code;
                city_name = map.get(Constants.COUNTY_NAME).toString();
            }
            if (!cityMap.containsKey(city_code)) {
                AreaEntity child = AreaEntity.builder().code(city_code).name(city_name).pcode(currentProvince.getCode()).build();
                cityMap.put(city_code, child);
                currentProvince.addChild(child);
            }
            if (!isCrossLevel) {
                //当前市
                AreaEntity currentCity = cityMap.get(city_code);
                if (!countyMap.containsKey(county_code)) {
                    String county_name = map.get(Constants.COUNTY_NAME).toString();
                    AreaEntity child = AreaEntity.builder().code(county_code).name(county_name).pcode(currentCity.getCode()).build();
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

    public static void recursionWrite(List<AreaEntity> areaEntityList) {
        areaEntityList.forEach(areaEntity -> {
            String code = areaEntity.getCode();
            MapResponse mapResponse = HttpUtils.get(code);
            if (StrUtil.equals("1", mapResponse.getStatus()) && StrUtil.equalsAnyIgnoreCase("ok", mapResponse.getInfo()) && StrUtil.equalsAnyIgnoreCase("10000", mapResponse.getInfocode())) {
                List<District> districts = mapResponse.getDistricts();
                if (CollectionUtil.isNotEmpty(districts)) {
                    List<Feature> kidFeatures = districts.stream().map(district -> buildFeature(district, areaEntity)).collect(Collectors.toList());
                    MapResultDto mapResultDto = buildGeometry(kidFeatures);
                    writeFeatureFile(mapResultDto, areaEntity.getCode());
                }

            }

            if (CollectionUtil.isNotEmpty(areaEntity.getChildren())) {
                recursionWrite(areaEntity.getChildren());
            }
        });

    }

    public static void recursionWriteFull(List<AreaEntity> areaEntityList) {
        areaEntityList.forEach(areaEntity -> {

            List<AreaEntity> childrens = areaEntity.getChildren();
            if (CollectionUtil.isEmpty(childrens)) {
                childrens = new ArrayList<>();
                childrens.add(areaEntity);
            }

            List<Feature> features = new ArrayList<>();

            childrens.stream().forEach(child -> {
                MapResponse mapResponse = HttpUtils.get(child.getCode());
                if (StrUtil.equals("1", mapResponse.getStatus()) && StrUtil.equalsAnyIgnoreCase("ok", mapResponse.getInfo()) && StrUtil.equalsAnyIgnoreCase("10000", mapResponse.getInfocode())) {
                    List<District> districts = mapResponse.getDistricts();
                    if (CollectionUtil.isNotEmpty(districts)) {
                        List<Feature> kidFeatures = districts.stream().map(district -> buildFeature(district, child)).collect(Collectors.toList());
                        features.addAll(kidFeatures);
                    }
                }else {
                    LogUtil.error("请求节点错误 请手动补偿： " + areaEntity.getName() +" -> "+child.getName());
                }
            });

            if (CollectionUtil.isNotEmpty(features)) {
                MapResultDto mapResultDto = buildGeometry(features);
                writeFeatureFileFull(mapResultDto, areaEntity.getCode() + "_full");
            }

            if (CollectionUtil.isNotEmpty(areaEntity.getChildren())) {
                recursionWriteFull(areaEntity.getChildren());
            }


        });

    }

    public static Feature buildFeature(District district, AreaEntity areaEntity) {
        String type = "Feature";
        Properties properties = new Properties();
        properties.setAdcode(district.getAdcode());
        properties.setName(district.getName());
        properties.setCenter(Arrays.stream(district.getCenter().split(",")).map(Double::parseDouble).collect(Collectors.toList()));
        properties.setCentroid(properties.getCenter());
        properties.setChildrenNum(CollectionUtil.isNotEmpty(areaEntity.getChildren()) ? areaEntity.getChildren().size() : 0);
        properties.setLevel(district.getLevel());
        Parent parent = new Parent();
        parent.setAdcode(areaEntity.getPcode());
        properties.setParent(parent);

        String polylineStr = district.getPolyline();
        String[] polylines = polylineStr.split("[|]");
        List<List<List<List<Double>>>> multiPolygon = Arrays.stream(polylines).map(polyline -> {
            String[] strings = polyline.split(";");
            List<List<Double>> line = Arrays.stream(strings).map(str -> {
                String[] pointstr = str.split(",");
                List<String> strPoint = Arrays.asList(pointstr);
                List<Double> point = strPoint.stream().map(Double::parseDouble).collect(Collectors.toList());
                return point;
            }).collect(Collectors.toList());
            List<Double> firstPoint = line.get(0);
            List<Double> lastPoint = line.get(line.size() - 1);
            // 线的起始点和终点没有重合 说明没有闭合 需要手动闭合
            if (firstPoint.get(0) != lastPoint.get(0) || firstPoint.get(1) != lastPoint.get(1)) {
                line.add(firstPoint);
            }
            List<List<List<Double>>> polygon = new ArrayList<>();
            polygon.add(line);
            return polygon;
        }).collect(Collectors.toList());

        Geometry geometry = new Geometry();
        geometry.setType("MultiPolygon");
        geometry.setCoordinates(multiPolygon);

        Feature feature = new Feature();
        feature.setType(type);
        feature.setProperties(properties);
        feature.setGeometry(geometry);
        return feature;
    }





    public static MapResultDto buildGeometry(List<Feature> features) {
        MapResultDto mapResultDto = new MapResultDto();
        mapResultDto.setType("FeatureCollection");
        mapResultDto.setFeatures(features);
        return mapResultDto;
    }

    public static void writeFeatureFile(MapResultDto mapResultDto, String fileName) {
        String path = featureDir + fileName + ".json";
        FileWriter fileWriter = new FileWriter(path);
        String content = JSONUtil.toJsonStr(mapResultDto);
        fileWriter.write(content);
    }

    public static void writeFeatureFileFull(MapResultDto mapResultDto, String fileName) {
        String path = featureDir + "full/" + fileName + ".json";
        FileWriter fileWriter = new FileWriter(path);
        String content = JSONUtil.toJsonStr(mapResultDto);
        fileWriter.write(content);
    }

    public static AreaEntity nodeByCode(List<AreaEntity> areaEntities, String code) {
        for (int i = 0; i < areaEntities.size(); i++) {
            AreaEntity areaEntity = areaEntities.get(i);
            if (StrUtil.equals(areaEntity.getCode(), code)) {
                return areaEntity;
            }
            if (CollectionUtil.isNotEmpty(areaEntity.getChildren())) {
                AreaEntity temp = nodeByCode(areaEntity.getChildren(), code);
                if (null != temp){
                    return temp;
                }
            }
        }
        return null;
    }
}
