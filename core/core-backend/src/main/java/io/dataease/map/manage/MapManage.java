package io.dataease.map.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.dataease.constant.CacheConstant.CommonCacheConstant.CUSTOM_GEO_CACHE;
import static io.dataease.constant.CacheConstant.CommonCacheConstant.WORLD_MAP_CACHE;

@Component
public class MapManage {
    private final static AreaNode WORLD;

    private static final String GEO_PREFIX = "geo_";
    private static final Pattern GEO_CODE_PATTERN = Pattern.compile("\\d{3}(\\d{6}|\\d{8})?");
    private static final String FEATURE_COLLECTION = "FeatureCollection";
    private static final String FEATURE = "Feature";
    private static final String GEOMETRY_COLLECTION = "GeometryCollection";
    private static final Set<String> GEOJSON_GEOMETRY_TYPES = Set.of(
            "Point",
            "MultiPoint",
            "LineString",
            "MultiLineString",
            "Polygon",
            "MultiPolygon",
            GEOMETRY_COLLECTION
    );

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
        if (ObjectUtils.isEmpty(request)) {
            DEException.throwException("geometry request is require");
        }
        // 统一外部区域编码
        String code = normalizeGeoCode(request.getCode());
        // 校验上传文件名和 JSON 后缀
        validateGeoUploadFile(file);
        // 校验 GeoJSON 内容结构
        JsonNode geoJson = parseGeoJson(file);
        List<Area> areas = proxy().defaultArea();

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
            if (isChina(code)) {
                writeGeoJsonToFile(geoJson, geoFile);
            } else {
                addGeoJsonField(code, geoJson, geoFile);
            }
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
            // 删除目标必须是安全路径解析生成
            Path file = buildGeoFile(id).toPath();
            try {
                Files.deleteIfExists(file);
            } catch (IOException e) {
                LogUtil.error(e.getMessage());
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
        return resolveGeoFilePath(code).toFile();
    }

    private File buildGeoFile(String code, boolean allowWorld) {
        return resolveGeoFilePath(code, allowWorld).toFile();
    }

    public void validateCode(String code) {
        normalizeGeoCode(code);
    }

    public boolean isNumeric(String str) {
        for (int i = str.length(); --i >= 0; ) {
            int chr = str.charAt(i);
            if (chr < 48 || chr > 57)
                return false;
        }
        return true;
    }

    /**
     * 将GeoJSON文件中的每个feature的properties添加adcode字段，值为根据父级code生成的子级code，并将修改后的GeoJSON写入指定文件。
     * @param code    当前行政区划编码
     * @param geoJson 上传的GeoJSON内容
     * @param geoFile 目标文件，修改后的GeoJSON将写入此文件
     * @throws IOException 如果读取或写入文件时发生错误
     */
    private void addGeoJsonField(String code, JsonNode geoJson, File geoFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode features = (ArrayNode) geoJson.get("features");
        for (JsonNode feature : features) {
            ObjectNode featureObj = (ObjectNode) feature;
            JsonNode propertiesNode = featureObj.get("properties");
            ObjectNode properties;
            if (propertiesNode instanceof ObjectNode) {
                properties = (ObjectNode) propertiesNode;
            } else {
                properties = mapper.createObjectNode();
                featureObj.set("properties", properties);
            }
            properties.put("adcode", setChildAdcode(code));
        }
        mapper.writeValue(geoFile, geoJson);
    }

    /**
     * 根据父级行政区划编码生成子级编码。 规则： 1. 若code为3位，直接在末尾加1并补0到9位； 2. 否则去除末尾所有0，最后一位数字加1，补0到9位； 3.
     * 若全为0，返回1并补0到原长度； 4. 若最后一位已为9，抛出“层级过长”异常。
     *
     * @param code 父级行政区划编码
     * @return 子级行政区划编码
     * @throws IllegalArgumentException 层级过长时抛出
     */
    private String setChildAdcode(String code) {
        // 3位时直接补1并补0到9位
        if (code.length() == 3) {
            return StringUtils.rightPad(code + "1", 9, '0');
        }
        // 去除末尾所有0
        String noTrailingZeros = StringUtils.stripEnd(code, "0");
        // 如果全是0，返回"1"并补0到原长度
        if (StringUtils.isBlank(noTrailingZeros)) {
            return StringUtils.rightPad("1", code.length(), '0');
        }
        if (noTrailingZeros.length() == 3) {
            return StringUtils.rightPad(noTrailingZeros + "1", 9, '0');
        }
        // 最后一位数字加1
        int lastDigit = noTrailingZeros.charAt(noTrailingZeros.length() - 1) - '0';
        if (lastDigit == 9) {
            throw new IllegalArgumentException("Hierarchy too deep");
        }
        String incremented = noTrailingZeros + (lastDigit + 1);
        // 补0到9位
        return StringUtils.rightPad(incremented, 9, '0');
    }

    public boolean isChina(String code) {
        return StringUtils.startsWith(getBusiGeoCode(code), "156");
    }

    /**
     * 根据前端传入的区域编码和地名映射信息，将映射结果写入对应的 GeoJSON 文件的根节点 deMapping 字段中
     */
    public void placeNameMapping(String id, Map<String, String> req) {
        // 地名映射读取已有世界地图文件时允许 000
        File file = buildGeoFile(id, true);
        if (!file.exists()) {
            DEException.throwException("GeoJSON 文件不存在: " + file.getAbsolutePath());
        }
        writeDeMappingToFile(file, req);
    }

    private void writeDeMappingToFile(File file, Map<String, String> req) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(file);
            validateGeoJson(root);
            ObjectNode objectNode = (ObjectNode) root;

            ObjectNode deMappingNode = mapper.createObjectNode();
            if (req != null && !req.isEmpty()) {
                req.forEach(deMappingNode::put);
            }
            objectNode.set("deMapping", deMappingNode);
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, objectNode);
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
            DEException.throwException(e);
        }
    }

    /**
     * 拒绝危险文件名并限制上传后缀为 JSON
     */
    private void validateGeoUploadFile(MultipartFile file) {
        if (ObjectUtils.isEmpty(file) || file.isEmpty()) {
            DEException.throwException("geometry file is require");
        }

        String filename = file.getOriginalFilename();
        FileUtils.validateUploadFilename(filename);
        String suffix = FileUtils.getExtensionName(filename);
        if (!StringUtils.equalsIgnoreCase("json", suffix)) {
            DEException.throwException("仅支持json格式文件");
        }
    }

    /**
     * 上传的地图文件内容必须是可解析的 GeoJSON
     */
    private JsonNode parseGeoJson(MultipartFile file) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode geoJson = mapper.readTree(file.getInputStream());
            validateGeoJson(geoJson);
            return geoJson;
        } catch (IOException e) {
            LogUtil.error(e.getMessage());
            DEException.throwException("GeoJSON 文件内容无效");
            return null;
        }
    }

    private void writeGeoJsonToFile(JsonNode geoJson, File geoFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(geoFile, geoJson);
    }

    /**
     * GeoJSON 内容写入风险 仅允许标准 FeatureCollection 结构
     */
    private void validateGeoJson(JsonNode geoJson) {
        if (!(geoJson instanceof ObjectNode)) {
            DEException.throwException("GeoJSON 根节点不是对象");
        }
        JsonNode type = geoJson.get("type");
        if (type == null || !type.isTextual() || !StringUtils.equals(FEATURE_COLLECTION, type.asText())) {
            DEException.throwException("仅支持 FeatureCollection 格式 GeoJSON");
        }
        JsonNode features = geoJson.get("features");
        if (!(features instanceof ArrayNode)) {
            DEException.throwException("GeoJSON features 必须是数组");
        }
        for (JsonNode feature : features) {
            validateGeoJsonFeature(feature);
        }
    }


    /**
     * 校验地图文件内容 GeoJSON 做 FeatureCollection 校验
     */
    private void validateGeoJsonFeature(JsonNode feature) {
        if (!(feature instanceof ObjectNode)) {
            DEException.throwException("GeoJSON feature 必须是对象");
        }
        JsonNode type = feature.get("type");
        if (type == null || !type.isTextual() || !StringUtils.equals(FEATURE, type.asText())) {
            DEException.throwException("GeoJSON feature 类型无效");
        }
        JsonNode geometry = feature.get("geometry");
        validateGeoJsonGeometry(geometry);
        JsonNode properties = feature.get("properties");
        if (properties != null && !properties.isNull() && !(properties instanceof ObjectNode)) {
            DEException.throwException("GeoJSON properties 必须是对象");
        }
    }


    /**
     * 校验 GeoJSON 里每个 Feature.geometry 是否合法
     */
    private void validateGeoJsonGeometry(JsonNode geometry) {
        if (!(geometry instanceof ObjectNode)) {
            DEException.throwException("GeoJSON geometry 必须是对象");
        }
        JsonNode type = geometry.get("type");
        if (type == null || !type.isTextual() || !GEOJSON_GEOMETRY_TYPES.contains(type.asText())) {
            DEException.throwException("GeoJSON geometry 类型无效");
        }
        if (StringUtils.equals(GEOMETRY_COLLECTION, type.asText())) {
            JsonNode geometries = geometry.get("geometries");
            if (!(geometries instanceof ArrayNode)) {
                DEException.throwException("GeoJSON geometries 必须是数组");
            }
            for (JsonNode item : geometries) {
                validateGeoJsonGeometry(item);
            }
            return;
        }
        JsonNode coordinates = geometry.get("coordinates");
        if (!(coordinates instanceof ArrayNode)) {
            DEException.throwException("GeoJSON coordinates 必须是数组");
        }
    }

    /**
     * 区域编码只允许 geo_ 前缀加受控长度数字或纯受控长度数字
     */
    private String normalizeGeoCode(String code) {
        return normalizeGeoCode(code, false);
    }

    private String normalizeGeoCode(String code, boolean allowWorld) {
        if (StringUtils.isBlank(code)) {
            DEException.throwException("区域编码不能为空");
        }
        String busiGeoCode = getBusiGeoCode(code);
        if (!GEO_CODE_PATTERN.matcher(busiGeoCode).matches()
                || (!allowWorld && StringUtils.equals(countryCodeOfValidated(busiGeoCode), WORLD.getId()))) {
            DEException.throwException("有效区域编码只能是3位、9位或11位数字");
        }
        return busiGeoCode;
    }

    private String countryCodeOfValidated(String code) {
        return code.substring(0, 3);
    }

    /**
     * 规范化目标路径并确认文件始终位于地图目录内
     */
    private Path resolveGeoFilePath(String code) {
        return resolveGeoFilePath(code, false);
    }

    private Path resolveGeoFilePath(String code, boolean allowWorld) {
        String id = normalizeGeoCode(code, allowWorld);
        String baseDir = isWorld(id) || isChina(id) ? StaticResourceConstants.MAP_DIR : StaticResourceConstants.CUSTOM_MAP_DIR;
        Path basePath = Paths.get(baseDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(basePath);
            Path realBasePath = basePath.toRealPath();
            Path countryPath = realBasePath.resolve(countryCodeOfValidated(id)).normalize();
            if (!countryPath.startsWith(realBasePath)) {
                DEException.throwException("非法地图文件路径");
            }
            Files.createDirectories(countryPath);
            Path realCountryPath = countryPath.toRealPath();
            if (!realCountryPath.startsWith(realBasePath)) {
                DEException.throwException("非法地图文件路径");
            }
            Path targetPath = realCountryPath.resolve(id + ".json").normalize();
            if (!targetPath.startsWith(realCountryPath) || Files.isSymbolicLink(targetPath)) {
                DEException.throwException("非法地图文件路径");
            }
            return targetPath;
        } catch (IOException e) {
            LogUtil.error(e.getMessage());
            DEException.throwException(e);
            return null;
        }
    }

    private boolean isWorld(String code) {
        return StringUtils.equals(getBusiGeoCode(code), WORLD.getId());
    }
}
